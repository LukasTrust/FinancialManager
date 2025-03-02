async function loadLineChart(
    messages: Record<string, string>,
    startDate: string | null = null,
    endDate: string | null = null
): Promise<void> {
    try {
        let url = `/bankAccountOverview/${bankAccountId}/data/lineChart`;
        const params = new URLSearchParams();

        if (startDate) params.append("startDate", startDate);
        if (endDate) params.append("endDate", endDate);

        if (params.toString()) {
            url += `?${params.toString()}`;
        }

        const response = await fetch(url, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            showAlert("ERROR", messages["error_loadingLineChart"]);
            return;
        }

        const responseBody: ChartData = await response.json();

        const bankName = document.getElementById("bankName");
        if (bankName) {
            bankName.innerText = responseBody.seriesList[0]?.name || "";
        }

        createLineChart(responseBody);
    } catch (error) {
        showAlert("ERROR", messages["error_generic"]);
        console.error("Error loading chart:", error);
    }
}

declare const Chart: any;

function createLineChart(responseBody: ChartData): void {
    const chartId = "lineChart";
    const chartContainer = document.getElementById(chartId) as HTMLCanvasElement | null;
    if (!chartContainer) {
        console.error("Chart container not found");
        return;
    }

    const ctx = chartContainer.getContext("2d");
    if (!ctx) {
        console.error("Could not get 2D context");
        return;
    }

    // Destroy previous chart instance if it exists
    if (existingChart) {
        existingChart.destroy();
    }

    const chartData = transformChartData(responseBody);
    const currency = getCurrentCurrencySymbol();

    existingChart = new Chart(ctx, {
        type: "line",
        data: chartData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: true },
                tooltip: {
                    callbacks: {
                        label: (context: any) => {
                            let label = context.dataset.label || "";
                            if (label) {
                                label += ": ";
                            }
                            if (context.parsed.y !== null) {
                                label += formatNumber(context.parsed.y, currency);
                            }
                            return label;
                        }
                    }
                }
            },
            scales: {
                x: {
                    type: "time",
                    time: {
                        unit: "month",
                        tooltipFormat: "dd MMM yyyy"
                    }
                }
            }
        }
    });
}

function transformChartData(responseBody: ChartData) {
    return {
        labels: responseBody.seriesList[0]?.dataPoints.map(dp => new Date(dp.date)) || [],
        datasets: responseBody.seriesList.map(series => ({
            label: series.name,
            data: series.dataPoints.map(dp => ({
                x: new Date(dp.date),
                y: dp.value,
                info: dp.info
            }))
        }))
    };
}
