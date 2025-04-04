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
    if (existingChart instanceof Chart) {
        existingChart.destroy();
    }
    const chartData = transformChartData(responseBody);
    const currency = getCurrentCurrencySymbol();
    const tooltipOptions = {
        backgroundColor: 'rgba(30, 30, 30, 0.9)',
        titleFont: { family: "'Poppins', sans-serif", size: 16, weight: 'bold' },
        bodyFont: { family: "'Poppins', sans-serif", size: 14 },
        padding: 14,
        usePointStyle: true,
        callbacks: {
            label: ({ raw }) => {
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
function transformChartData(responseBody) {
    var _a;
    return {
        labels: ((_a = responseBody.seriesList[0]) === null || _a === void 0 ? void 0 : _a.dataPoints.map(dp => new Date(dp.date))) || [],
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