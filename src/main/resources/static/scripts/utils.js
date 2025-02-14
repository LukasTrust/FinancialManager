function createElement(type, className, textContent = '', attributes = {}) {
    const element = document.createElement(type);
    if (className) element.className = className;
    if (textContent) element.textContent = textContent;
    Object.entries(attributes).forEach(([key, value]) => element.setAttribute(key, value));
    return element;
}

function getCurrentCurrencySymbol() {
    return " " + bankAccountSymbols[bankAccountId];
}

function formatDateString(date) {
    // Split the input date string into year, month, and day
    const [year, month, day] = date.split('-');

    // Create a Date object (note: months are 0-indexed in JavaScript)
    const dateObj = new Date(year, month - 1, day);

    // Get the day, month abbreviation, and year
    const formattedDay = dateObj.getDate();
    const formattedMonth = monthAbbreviations[dateObj.getMonth()];
    const formattedYear = dateObj.getFullYear();

    // Return the formatted date string
    return `${formattedDay} ${formattedMonth} ${formattedYear}`;
}

function formatNumber(number, currency) {
    const formattedNumber = Number(number).toFixed(2).replace('.', ',');

    // Combine the formatted number with the currency symbol
    return `${formattedNumber} ${currency}`;
}