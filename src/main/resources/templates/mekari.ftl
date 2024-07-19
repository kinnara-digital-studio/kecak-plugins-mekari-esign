<!DOCTYPE html>
<html lang="en">
<head>
    <style>
    </style>
</head>
<body>
    <a href="${serverUrl}/auth?client_id=${clientId}&response_type=code&scope=esign&lang=id">
        <button>Login Mekari</button>
    </a>
    <script>
        const url = 'https://sandbox.kecak.org/web/json/plugin/com.kinnarastudio.kecakplugins.mekariesign.webservice.MekariESignWebhook/';
        fetch(url);
    </script>
</body>
</html>
