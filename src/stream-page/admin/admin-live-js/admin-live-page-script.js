import { initializeStreams, handleStreamFormSubmit, nextStreams, prevStreams } from './streamManager.js';
import { showModal, closeModal, closeModalOnOutsideClick } from './modalHandler.js';

document.addEventListener("DOMContentLoaded", function() {
    initializeStreams();

    document.getElementById('openModalButton').addEventListener('click', showModal);
    document.querySelector('.close-button').addEventListener('click', closeModal);

    window.addEventListener('click', closeModalOnOutsideClick);

    document.getElementById('streamForm').addEventListener('submit', handleStreamFormSubmit);
    document.getElementById('prevStreamsButton').addEventListener('click', prevStreams);
    document.getElementById('nextStreamsButton').addEventListener('click', nextStreams);
});
