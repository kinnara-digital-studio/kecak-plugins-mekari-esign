<!DOCTYPE html>
<html lang="en">

<head>
    
</head>

<body>
    
    <div class="form-cell" ${elementMetaData!}>
        <h2>Anda belum Login ke Mekari eSign, silakan Login di sini:</h2>
        <br>
        <a href="${serverUrl}/auth?client_id=${clientId}&response_type=code&scope=esign&lang=id">
            <button>Login Mekari</button>
        </a>
    </div>
</body>

</html>