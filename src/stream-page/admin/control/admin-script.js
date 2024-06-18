document.addEventListener('DOMContentLoaded', function() {
    const accessToken = localStorage.getItem('accessToken');

    const manageUsersLink = document.getElementById('manageUsersLink');
    const manageStreamsLink = document.getElementById('manageStreamsLink');
    const userManagementSection = document.getElementById('userManagement');
    const streamManagementSection = document.getElementById('streamManagement');

    manageUsersLink.addEventListener('click', () => {
        userManagementSection.style.display = 'block';
        streamManagementSection.style.display = 'none';
    });

    manageStreamsLink.addEventListener('click', () => {
        userManagementSection.style.display = 'none';
        streamManagementSection.style.display = 'block';
    });

    const addUserButton = document.getElementById('addUserButton');
    const userFormModal = document.getElementById('userFormModal');
    const closeModalButton = document.querySelector('.close-button');
    const userForm = document.getElementById('userForm');
    const userList = document.getElementById('userList').getElementsByTagName('tbody')[0];

    addUserButton.addEventListener('click', () => {
        userFormModal.style.display = 'block';
    });

    closeModalButton.addEventListener('click', () => {
        userFormModal.style.display = 'none';
    });

    window.addEventListener('click', (event) => {
        if (event.target === userFormModal) {
            userFormModal.style.display = 'none';
        }
    });

    userForm.addEventListener('submit', async function(event) {
        event.preventDefault();

        const formData = new FormData(userForm);
        const user = {};
        formData.forEach((value, key) => {
            if (key === 'role') {
                user['roles'] = [value];
            } else {
                user[key] = value;
            }
        });

        try {
            const response = await fetch('http://localhost:8080/api/admin/users/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + accessToken
                },
                body: JSON.stringify(user)
            });

            if (response.ok) {
                userFormModal.style.display = 'none';
                fetchUsers();
            } else {
                console.error('Failed to register user');
            }
        } catch (error) {
            console.error('Error:', error);
        }
    });

    const fetchUsers = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/admin/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + accessToken
                },
                body: JSON.stringify({ page: 0, size: 50 })
            });

            if (response.ok) {
                const users = await response.json();
                displayUsers(users);
            } else {
                console.error('Failed to fetch users');
                displayUsers([]);
            }
        } catch (error) {
            console.error('Error:', error);
            displayUsers([]);
        }
    };

    const displayUsers = (users) => {
        userList.innerHTML = '';
        if (users.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.innerHTML = `<td colspan="6">No users found.</td>`;
            userList.appendChild(emptyRow);
        } else {
            users.forEach(user => {
                const userItem = document.createElement('tr');
                userItem.className = 'user-item';
                userItem.innerHTML = `
                    <td>${user.firstName}</td>
                    <td>${user.secondName}</td>
                    <td>${user.middleName}</td>
                    <td>${user.username}</td>
                    <td>${user.roles.map(role => role.roleType).join(', ')}</td>
                    <td class="action-buttons">
                        <button onclick="deleteUser(${user.id}, '${user.username}')"><i class="fas fa-trash-alt"></i></button>
                        <button onclick="addRole(${user.id})"><i class="fas fa-user-plus"></i></button>
                        <button onclick="removeRole(${user.id})"><i class="fas fa-user-minus"></i></button>
                    </td>
                `;
                userList.appendChild(userItem);
            });
        }
    };

    const fetchStreams = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/streamPage/streams', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + accessToken
                },
                body: JSON.stringify({ page: 0, size: 50 })
            });

            if (response.ok) {
                const streams = await response.json();
                displayStreams(streams);
            } else {
                console.error('Failed to fetch streams');
                displayStreams([]);
            }
        } catch (error) {
            console.error('Error:', error);
            displayStreams([]);
        }
    };

    const displayStreams = (streams) => {
        const streamList = document.getElementById('streamList').getElementsByTagName('tbody')[0];
        streamList.innerHTML = '';
        if (streams.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.innerHTML = `<td colspan="5">No streams found.</td>`;
            streamList.appendChild(emptyRow);
        } else {
            streams.forEach(stream => {
                const streamItem = document.createElement('tr');
                streamItem.className = 'stream-item';
                streamItem.innerHTML = `
                    <td>${stream.streamName}</td>
                    <td>${stream.groupName}</td>
                    <td>${stream.rtspUrl}</td>
                    <td>${stream.record ? 'Yes' : 'No'}</td>
                    <td class="action-buttons">
                        <button onclick="deleteStream(${stream.id})"><i class="fas fa-trash-alt"></i></button>
                    </td>
                `;
                streamList.appendChild(streamItem);
            });
        }
    };

    window.deleteStream = async (streamId) => {
        if (confirm('Are you sure you want to delete this stream?')) {
            try {
                const response = await fetch(`http://localhost:8080/api/streamPage/controlStream/${streamId}/delete`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': 'Bearer ' + accessToken
                    }
                });

                if (response.ok) {
                    fetchStreams();
                } else {
                    console.error('Failed to delete stream');
                }
            } catch (error) {
                console.error('Error:', error);
            }
        }
    };

    fetchUsers();
    fetchStreams();

    const addStreamButton = document.getElementById('addStreamButton');
    const streamFormModal = document.getElementById('streamFormModal');
    const closeStreamModalButton = streamFormModal.querySelector('.close-button');
    const streamForm = document.getElementById('streamForm');

    addStreamButton.addEventListener('click', () => {
        streamFormModal.style.display = 'block';
    });

    closeStreamModalButton.addEventListener('click', () => {
        streamFormModal.style.display = 'none';
    });

    window.addEventListener('click', (event) => {
        if (event.target === streamFormModal) {
            streamFormModal.style.display = 'none';
        }
    });

    streamForm.addEventListener('submit', async function(event) {
        event.preventDefault();

        const rtspUrl = document.getElementById('rtspUrl').value;
        const streamName = document.getElementById('streamName').value;
        const record = document.getElementById('record').checked;
        const groupName = document.getElementById('groupName').value;

        document.getElementById('loadingModal').style.display = 'block';
        let errorOccurred = false;
        let errorMessage = 'Unknown error';
        const timeoutId = setTimeout(() => {
            errorOccurred = true;
            errorMessage = 'Timeout: Unable to get stream after 10 seconds';
            handleError();
        }, 10000);

        try {
            const response = await fetch('http://localhost:8080/api/streamPage/controlStream/startStream', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + accessToken,
                },
                body: JSON.stringify({ rtspUrl, streamName, record, groupName })
            });

            clearTimeout(timeoutId);

            if (response.ok) {
                fetchStreams();
                showSuccess('Stream added successfully.');
            } else {
                errorOccurred = true;
                errorMessage = `Error starting stream: ${response.statusText}`;
            }
        } catch (error) {
            clearTimeout(timeoutId);
            errorOccurred = true;
            errorMessage = `Error: ${error.message}`;
        }

        if (errorOccurred) {
            handleError();
        }

        function handleError() {
            document.getElementById('loadingModal').style.display = 'none';
            document.getElementById('loadingStatus').style.display = 'block';
            document.getElementById('successStatus').style.display = 'none';
            document.getElementById('errorStatus').innerText = errorMessage;
            document.getElementById('errorStatus').style.display = 'block';
            setTimeout(() => {
                document.getElementById('loadingModal').style.display = 'none';
            }, 2000);
        }

        function showSuccess(message) {
            document.getElementById('loadingStatus').style.display = 'none';
            document.getElementById('successStatus').innerText = message;
            document.getElementById('successStatus').style.display = 'block';
            setTimeout(() => {
                document.getElementById('loadingModal').style.display = 'none';
                document.getElementById('loadingStatus').style.display = 'block';
                document.getElementById('successStatus').style.display = 'none';
            }, 2000);
        }
    });
});

const deleteUser = async (userId, username) => {
    const accessToken = localStorage.getItem('accessToken');
    const currentUsername = localStorage.getItem('username'); // предполагается, что текущее имя пользователя сохранено в localStorage

    console.log(`Trying to delete user: ${username} (current user: ${currentUsername})`); // Отладочное сообщение

    if (username === currentUsername) {
        if (!confirm('You are about to delete yourself. Are you sure you want to proceed?')) {
            return;
        }
    }

    try {
        const response = await fetch(`http://localhost:8080/api/admin/users/${userId}/delete`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + accessToken
            }
        });

        if (response.ok) {
            fetchUsers();
        } else {
            console.error('Failed to delete user');
        }
    } catch (error) {
        console.error('Error:', error);
    }
};

const addRole = async (userId) => {
    const accessToken = localStorage.getItem('accessToken');
    const role = prompt('Enter role to add (USER or ADMIN):');
    if (role) {
        try {
            const response = await fetch(`http://localhost:8080/api/admin/users/${userId}/addRole`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + accessToken
                },
                body: JSON.stringify({ roleType: role })
            });

            if (response.ok) {
                fetchUsers();
            } else {
                console.error('Failed to add role');
            }
        } catch (error) {
            console.error('Error:', error);
        }
    }
};

const removeRole = async (userId) => {
    const accessToken = localStorage.getItem('accessToken');
    const role = prompt('Enter role to remove (USER or ADMIN):');
    if (role) {
        try {
            const response = await fetch(`http://localhost:8080/api/admin/users/${userId}/removeRole`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + accessToken
                },
                body: JSON.stringify({ roleType: role })
            });

            if (response.ok) {
                fetchUsers();
            } else {
                console.error('Failed to remove role');
            }
        } catch (error) {
            console.error('Error:', error);
        }
    }
};



