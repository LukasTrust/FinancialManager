let existingChart = null;

function createLineChart(responseBody) {
    const chartId = "lineChart";

    const chartContainer = document.getElementById(chartId);
    if (!chartContainer) {
        console.error("Chart container not found");
        return;
    }

    const ctx = chartContainer.getContext("2d");

    // Destroy previous chart instance if it exists
    if (existingChart) {
        existingChart.destroy();
    }

    const chartData = transformChartData(responseBody);

    existingChart = new Chart(ctx, {
        type: 'line',
        data: chartData,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: true }
            },
            scales: {
                x: {
                    type: 'time',
                    time: {
                        unit: 'month',
                        tooltipFormat: 'dd MMM yyyy'
                    },
                },
            }
        }
    });
}

function transformChartData(responseBody) {
    return {
        labels: responseBody.seriesList[0].dataPoints.map(dp => dp.date),
        datasets: responseBody.seriesList.map(series => ({
            label: series.name,
            data: series.dataPoints.map(dp => ({ x: dp.date, y: dp.value, info: dp.info })),
        }))
    };
}