async function loadContracts(messages) {
    try {
        const response = await fetch(`/contracts/${bankAccountId}/data`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const responseBody = await response.json();
            showAlert(responseBody.alertType, responseBody.message);

            showAlert("ERROR", messages["error_loadingContracts"]);
            return;
        }

        contractData = await response.json();

    } catch (error) {
        console.error("There was an error loading the contracts:", error);
        showAlert('error', messages["error_generic"]);
    }
}