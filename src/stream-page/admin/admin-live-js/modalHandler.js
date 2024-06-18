export const showModal = () => {
    document.getElementById('streamModal').style.display = 'block';
};

export const closeModal = () => {
    document.getElementById('streamModal').style.display = 'none';
};

export const closeModalOnOutsideClick = (event) => {
    if (event.target === document.getElementById('streamModal')) {
        document.getElementById('streamModal').style.display = 'none';
    }
};
