$(document).ready(function() {
    const path = window.location.pathname;
    const commonPath = getCommonPath(path);

    // Подключение навигации
    $("#navigation").load(commonPath + "common.html", function() {
        setNavigationActiveState(path);
    });

    // Логика выхода из аккаунта
    $(document).on('click', '#logoutLink', function() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('username');
        window.location.href = commonPath + '../auth/login.html';
    });
});

function getCommonPath(currentPath) {
    if (currentPath.includes('/control/')) {
        return '../../../common/';
    } else if (currentPath.includes('/admin-live-js/')) {
        return '../../common/';
    } else if (currentPath.includes('/download-video/')) {
        return '../common/';
    } else if (currentPath.includes('/user/')) {
        return '../../common/';
    } else {
        return '../../common/';
    }
}

function setNavigationActiveState(path) {
    const userRole = localStorage.getItem('userRole');
    if (path.includes('admin-live-page.html')) {
        $('#homeLink').attr('href', path).addClass('active');
        $('#downloadLink').attr('href', '../../download-video/download-video.html');
        $('#settingsLink').attr('href', '../admin/control/admin-page.html');
    } else if (path.includes('user-live-page.html')) {
        $('#homeLink').attr('href', path).addClass('active');
        $('#downloadLink').attr('href', '../../download-video/download-video.html');
        $('#settingsLink').hide();
    } else if (path.includes('admin-page.html')) {
        $('#settingsLink').attr('href', path).addClass('active');
        $('#homeLink').attr('href', '../admin-live-page.html');
        $('#downloadLink').attr('href', '../../../download-video/download-video.html');
    } else if (path.includes('download-video.html')) {
        $('#downloadLink').attr('href', path).addClass('active');
        if (userRole === 'admin') {
            $('#homeLink').attr('href', '../stream-page/admin/admin-live-page.html');
            $('#settingsLink').attr('href', '../stream-page/admin/control/admin-page.html');
        } else {
            $('#homeLink').attr('href', '../stream-page/user/user-live-page.html');
            $('#settingsLink').hide();
        }
    }
}
