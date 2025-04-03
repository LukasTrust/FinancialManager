async function buildDashboard() {
    const messages = await loadLocalization("dashboard");
    if (!messages) return;

    handleDateRangeSelection(messages, false);
    await updateVisuals(messages, false);
}