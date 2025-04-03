async function loadLineChart(
    messages: Record<string, string>,
    startDate: string | null = null,
    endDate: string | null = null,
    solo: boolean
): Promise<void> {
    try {
        let url = solo == true ? `/bankAccountOverview/${bankAccountId}/data/lineChart` :  `/dashboard/data/lineChart`;
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

        console.log(responseBody);

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
    if (existingChart instanceof Chart) {
        existingChart.destroy();
    }

    const chartData = transformChartData(responseBody);
    const currency = getCurrentCurrencySymbol();

    const tooltipOptions: TooltipOptions = {
        backgroundColor: 'rgba(30, 30, 30, 0.9)',
        titleFont: { family: "'Poppins', sans-serif", size: 16, weight: 'bold' },
        bodyFont: { family: "'Poppins', sans-serif", size: 14 },
        padding: 14,
        usePointStyle: true,
        callbacks: {
            label: ({ raw }: any) => {
                return `    ${raw.counterPartyName}    ${formatNumber(raw.amount, currency)}`;
            }
        }
    };

    const axisTicks = {
        color: '#94A3B8',
        font: { family: "'Poppins', sans-serif", size: 12 }
    };

    existingChart = new Chart(ctx, {
        type: "line",
        data: chartData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { intersect: false, mode: 'nearest', axis: 'x' },
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                    labels: { usePointStyle: true, padding: 20, font: { family: "'Poppins', sans-serif", size: 14 } }
                },
                tooltip: tooltipOptions
            },
            scales: {
                x: {
                    type: "time",
                    time: { unit: "month", tooltipFormat: "dd MMM yyyy" },
                    grid: { display: false, drawBorder: false },
                    ticks: axisTicks
                },
                y: {
                    grid: { color: 'rgba(0, 0, 0, 0.1)', drawBorder: false, lineWidth: 1 },
                    ticks: axisTicks
                }
            },
        }
    });
}

function transformChartData(responseBody: ChartData) {
    return {
        labels: responseBody.seriesList[0]?.dataPoints.map(dp => new Date(dp.date)) || [],
        datasets: responseBody.seriesList.map(series => ({
            label: series.name,
            fill: false, // Ensure missing values don't connect
            data: series.dataPoints.map(dp => ({
                x: new Date(dp.date),
                y: dp.value,
                info: dp.info,
                counterPartyName: dp.counterPartyName,
                amount: dp.amount,
                pointStyle: dp.style
            }))
        }))
    };
}

function handleDateRangeSelection(messages: Record<string, string>, solo: boolean): void {
    const startDate = document.getElementById("startDate") as HTMLInputElement;
    const endDate = document.getElementById("endDate") as HTMLInputElement;
    const clearDateButton = document.getElementById("clearDateButton") as HTMLButtonElement;

    startDate.addEventListener("input", async () => await checkDates(messages, solo, startDate, endDate));
    endDate.addEventListener("input", async () => await checkDates(messages, solo, startDate, endDate));
    clearDateButton.addEventListener("click", async () => {
        startDate.value = '';
        endDate.value = '';
        await updateVisuals(messages, solo);
    });
}

async function checkDates(messages: Record<string, string>, solo: boolean, startDate: HTMLInputElement, endDate: HTMLInputElement): Promise<void> {
    const startValue = startDate.value.trim() || null;
    const endValue = endDate.value.trim() || null;

    if (startValue || endValue) {
        await updateVisuals(messages, solo, startValue, endValue);
    }
}

async function updateVisuals(messages: Record<string, string>, solo: boolean, startDate: string | null = null, endDate: string | null = null): Promise<void> {
    await loadLineChart(messages, startDate, endDate, solo);
    await loadKeyFigures(messages, startDate, endDate, solo);
}