const accessToken = localStorage.getItem('accessToken');
const existingStreams = new Set();
let allStreams = [];
let filteredStreams = [];
let displayedStreams = [];
const maxStreams = 6;
let currentStreamIndex = 0;
let selectedGroup = 'all';

document.addEventListener('DOMContentLoaded', function() {
    initializeStreams();

    const groupFilter = document.getElementById('groupFilter');
    groupFilter.addEventListener('change', (event) => {
        selectedGroup = event.target.value;
        filterStreamsByGroup();
    });

    document.getElementById('prevStreamsButton').addEventListener('click', prevStreams);
    document.getElementById('nextStreamsButton').addEventListener('click', nextStreams);
});

const initializeStreams = () => {
    fetch('http://localhost:8080/api/streamPage/streams', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + accessToken
        },
        body: JSON.stringify({ page: 0, size: 50 })
    })
        .then(response => {
            if (!response.ok) {
                updateNavigationButtons();
                throw new Error('Failed to fetch streams');
            }
            return response.json();
        })
        .then(data => {
            allStreams = data;
            filteredStreams = allStreams;
            displayedStreams = filteredStreams.slice(0, maxStreams);
            displayedStreams.forEach(stream => addStream(stream.dashUrl, stream.streamName));
            populateGroupFilter(allStreams);
            updateNavigationButtons();
        })
        .catch(error => {
            updateNavigationButtons();
            console.error('Error:', error);
        });
};

const populateGroupFilter = (streams) => {
    const groupFilter = document.getElementById('groupFilter');
    const uniqueGroups = [...new Set(streams.map(stream => stream.groupName))];
    uniqueGroups.forEach(group => {
        const option = document.createElement('option');
        option.value = group;
        option.textContent = group;
        groupFilter.appendChild(option);
    });
};

const filterStreamsByGroup = () => {
    if (selectedGroup === 'all') {
        filteredStreams = allStreams;
    } else {
        filteredStreams = allStreams.filter(stream => stream.groupName === selectedGroup);
    }
    currentStreamIndex = 0;
    displayCurrentStreams();
    updateNavigationButtons();
};

const addStream = (dashUrl, streamName) => {
    const streamsContainer = document.getElementById('streamsContainer');
    const streamDiv = document.createElement('div');
    streamDiv.className = 'stream';

    const video = document.createElement('video');
    video.muted = true;
    video.autoplay = true;
    video.setAttribute('data-dashjs-player', '');

    const buttonContainer = document.createElement('div');
    buttonContainer.className = 'button-container';

    const muteButton = document.createElement('button');
    muteButton.className = 'mute-button';
    muteButton.innerText = 'Unmute';
    muteButton.addEventListener('click', () => {
        video.muted = !video.muted;
        muteButton.innerText = video.muted ? 'Unmute' : 'Mute';
    });

    const refreshButton = document.createElement('button');
    refreshButton.className = 'refresh-button';
    refreshButton.innerText = 'Refresh';
    refreshButton.addEventListener('click', () => {
        player.attachSource(dashUrl);
    });

    buttonContainer.appendChild(muteButton);
    buttonContainer.appendChild(refreshButton);

    streamDiv.appendChild(video);
    streamDiv.appendChild(buttonContainer);
    streamsContainer.appendChild(streamDiv);

    const player = dashjs.MediaPlayer().create();
    player.updateSettings({
        'streaming': {
            'liveDelay': 3,
            'liveCatchup': {
                'enabled': true
            }
        }
    });

    player.extend("RequestModifier", function() {
        return {
            modifyRequestHeader: function(xhr) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + accessToken);
                return xhr;
            }
        };
    }, true);

    player.initialize(video, dashUrl, true);

    player.on(dashjs.MediaPlayer.events.ERROR, function (e) {
        console.error('Error code', e.error.code, ' - ', e.error.message);
    });
    player.on(dashjs.MediaPlayer.events.STREAM_INITIALIZED, function () {
        console.log('Stream initialized for URL: ' + dashUrl);
    });
    player.on(dashjs.MediaPlayer.events.MANIFEST_LOADED, function () {
        console.log('Manifest loaded for URL: ' + dashUrl);
    });
    player.setAutoPlay(true);
    video.controls = false;

    adjustStreamLayout();
};

const adjustStreamLayout = () => {
    const streams = document.querySelectorAll('.stream');
    const streamCount = streams.length;

    if (streamCount === 1) {
        streams.forEach(stream => stream.style.width = '100%');
    } else if (streamCount === 2) {
        streams.forEach(stream => stream.style.width = '50%');
    } else {
        streams.forEach(stream => stream.style.width = '33.33%');
    }
};

const nextStreams = () => {
    if (currentStreamIndex + maxStreams < filteredStreams.length) {
        currentStreamIndex += maxStreams;
        displayCurrentStreams();
        updateNavigationButtons();
    }
};

const prevStreams = () => {
    if (currentStreamIndex - maxStreams >= 0) {
        currentStreamIndex -= maxStreams;
        displayCurrentStreams();
        updateNavigationButtons();
    }
};

const displayCurrentStreams = () => {
    const streamsContainer = document.getElementById('streamsContainer');
    streamsContainer.innerHTML = '';
    displayedStreams = filteredStreams.slice(currentStreamIndex, currentStreamIndex + maxStreams);
    displayedStreams.forEach(stream => addStream(stream.dashUrl, stream.streamName));
    updateNavigationButtons();  // Ensure buttons are updated after displaying streams
};

const updateNavigationButtons = () => {
    const prevButton = document.getElementById('prevStreamsButton');
    const nextButton = document.getElementById('nextStreamsButton');

    if (filteredStreams.length === 0 || filteredStreams.length <= maxStreams) {
        prevButton.style.display = 'none';
        nextButton.style.display = 'none';
    } else {
        prevButton.style.display = 'block';
        nextButton.style.display = 'block';
    }

    prevButton.disabled = currentStreamIndex === 0;
    nextButton.disabled = currentStreamIndex + maxStreams >= filteredStreams.length;
};
