export const showSuccess = (message) => {
    document.getElementById('loadingStatus').style.display = 'none';
    document.getElementById('successStatus').innerText = message;
    document.getElementById('successStatus').style.display = 'block';

    setTimeout(() => {
        document.getElementById('loadingModal').style.display = 'none';
        document.getElementById('loadingStatus').style.display = 'block';
        document.getElementById('successStatus').style.display = 'none';
    }, 2000);
};

export const showError = (message) => {
    document.getElementById('loadingStatus').style.display = 'none';
    document.getElementById('errorStatus').innerText = message;
    document.getElementById('errorStatus').style.display = 'block';

    setTimeout(() => {
        document.getElementById('loadingModal').style.display = 'none';
        document.getElementById('loadingStatus').style.display = 'block';
        document.getElementById('errorStatus').style.display = 'none';
    }, 2000);
};
