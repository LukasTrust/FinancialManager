async function loadLineChart(messages, startDate = null, endDate = null, solo) {
    var _a;
    try {
        let url = solo == true ? `/bankAccountOverview/${bankAccountId}/data/lineChart` : `/dashboard/data/lineChart`;
        const params = new URLSearchParams();
        if (startDate)
            params.append("startDate", startDate);
        if (endDate)
            params.append("endDate", endDate);
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
        const responseBody = await response.json();
        console.log(responseBody);
        const bankName = document.getElementById("bankName");
        if (bankName) {
            bankName.innerText = ((_a = responseBody.seriesList[0]) === null || _a === void 0 ? void 0 : _a.name) || "";
        }
        createLineChart(responseBody);
    }
    catch (error) {
        showAlert("ERROR", messages["error_generic"]);
        console.error("Error loading chart:", error);
    }
}
function createLineChart(responseBody) {
    const chartId = "lineChart";
    const chartContainer = document.getElementById(chartId);
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
        existingChart = null;
    }
    const chartData = transformChartData(responseBody);
    const currency = getCurrentCurrencySymbol();
    existingChart = new Chart(ctx, {
        type: "line",
        data: chartData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                intersect: false,
                mode: 'index',
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        font: {
                            family: "'Inter', sans-serif",
                            size: 12
                        }
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleFont: {
                        family: "'Inter', sans-serif",
                        size: 14,
                        weight: 'bold'
                    },
                    bodyFont: {
                        family: "'Inter', sans-serif",
                        size: 12
                    },
                    padding: 12,
                    usePointStyle: true,
                    callbacks: {
                        label: (context) => {
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
                    },
                    grid: {
                        display: false,
                        drawBorder: false
                    },
                    ticks: {
                        color: '#6B7280',
                        font: {
                            family: "'Inter', sans-serif"
                        }
                    }
                },
                y: {
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)',
                        drawBorder: false
                    },
                    ticks: {
                        color: '#6B7280',
                        font: {
                            family: "'Inter', sans-serif"
                        }
                    }
                }
            },
            elements: {
                point: {
                    hoverRadius: 8,
                    hoverBorderWidth: 2
                }
            }
        }
    });
}
function transformChartData(responseBody) {
    var _a;
    return {
        labels: ((_a = responseBody.seriesList[0]) === null || _a === void 0 ? void 0 : _a.dataPoints.map(dp => new Date(dp.date))) || [],
        datasets: responseBody.seriesList.map(series => ({
            label: series.name,
            data: series.dataPoints.map(dp => ({
                x: new Date(dp.date),
                y: dp.value,
                info: dp.info,
                pointStyle: dp.style
            })),
            borderWidth: 2
        }))
    };
}
function handleDateRangeSelection(messages, solo) {
    const startDate = document.getElementById("startDate");
    const endDate = document.getElementById("endDate");
    const clearDateButton = document.getElementById("clearDateButton");
    startDate.addEventListener("input", async () => await checkDates(messages, solo, startDate, endDate));
    endDate.addEventListener("input", async () => await checkDates(messages, solo, startDate, endDate));
    clearDateButton.addEventListener("click", async () => {
        startDate.value = '';
        endDate.value = '';
        await updateVisuals(messages, solo);
    });
}
async function checkDates(messages, solo, startDate, endDate) {
    const startValue = startDate.value.trim() || null;
    const endValue = endDate.value.trim() || null;
    if (startValue || endValue) {
        await updateVisuals(messages, solo, startValue, endValue);
    }
}
async function updateVisuals(messages, solo, startDate = null, endDate = null) {
    await loadLineChart(messages, startDate, endDate, solo);
    await loadKeyFigures(messages, startDate, endDate, solo);
}
//# sourceMappingURL=chart.js.map