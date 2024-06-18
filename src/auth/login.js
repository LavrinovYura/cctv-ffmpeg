document.getElementById("loginForm").addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent the form from submitting normally

    // Get the form data
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    // Create an object from the form data
    const loginData = {
        username: username,
        password: password
    };

    // Send a POST request to the server
    fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(loginData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to login');
            }
            return response.json();
        })
        .then(data => {
            // Save tokens and user info to localStorage
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            localStorage.setItem('username', username);  // Save username to localStorage

            // Save user name details for further use
            localStorage.setItem('firstName', data.firstName);
            localStorage.setItem('secondName', data.secondName);
            localStorage.setItem('middleName', data.middleName);

            // Check user roles and redirect accordingly
            const admin = data.admin;
            if (admin) {
                localStorage.setItem('userRole', 'admin')
                window.location.href = '../stream-page/admin/admin-live-page.html';  // Redirect to control streams page for ADMIN
            } else {
                localStorage.setItem('userRole', 'user')
                window.location.href = '../stream-page/user/user-live-page.html';  // Redirect to live page for USER
            }
            console.log('Login successful, redirected based on role');
        })
        .catch(error => {
            console.error('Error:', error);
        });
});
