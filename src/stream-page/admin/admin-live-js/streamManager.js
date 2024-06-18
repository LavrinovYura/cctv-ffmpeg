import {showSuccess, showError} from './notificationHandler.js';

const accessToken = localStorage.getItem('accessToken');
const existingStreams = new Set();
let allStreams = [];
let filteredStreams = [];
let displayedStreams = [];
const maxStreams = 6;
let currentStreamIndex = 0;
let selectedGroup = 'all';

export const initializeStreams = () => {
    fetch('http://localhost:8080/api/streamPage/streams', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + accessToken
        },
        body: JSON.stringify({page: 0, size: 50})
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

    groupFilter.addEventListener('change', (event) => {
        selectedGroup = event.target.value;
        filterStreamsByGroup();
    });

    const groupNameInput = document.getElementById('groupName');
    groupNameInput.addEventListener('input', () => {
        const datalist = document.getElementById('groupNames');
        datalist.innerHTML = '';
        uniqueGroups.forEach(group => {
            if (group.toLowerCase().includes(groupNameInput.value.toLowerCase())) {
                const option = document.createElement('option');
                option.value = group;
                datalist.appendChild(option);
            }
        });
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

export const handleStreamFormSubmit = async (event) => {
    event.preventDefault();

    const rtspUrl = document.getElementById('rtspUrl').value;
    const streamName = document.getElementById('streamName').value;
    const record = document.getElementById('record').checked;
    const groupName = document.getElementById('groupName').value;

    document.getElementById('streamModal').style.display = 'none';
    document.getElementById('loadingModal').style.display = 'block';

    if (existingStreams.has(rtspUrl)) {
        showError('Stream already exists.');
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/streamPage/controlStream/startStream', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + accessToken,
            },
            body: JSON.stringify({rtspUrl, streamName, record, groupName})
        });

        if (response.ok) {
            const newStream = await response.json();
            existingStreams.add(rtspUrl);

            allStreams.push(newStream);
            populateGroupFilter(allStreams);  // Update group filter with new stream's group
            filterStreamsByGroup();
            updateNavigationButtons();
            showSuccess('Stream added successfully.');
        } else {
            updateNavigationButtons();
            showError(`Error starting stream: ${response.statusText}`);
        }
    } catch (error) {
        showError(`Error: ${error.message}`);
    }
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
        player.attachSource(dashUrl); // Перезагрузка источника видео
    });

    buttonContainer.appendChild(muteButton);
    buttonContainer.appendChild(refreshButton);

    streamDiv.appendChild(video);
    streamDiv.appendChild(buttonContainer);
    streamsContainer.appendChild(streamDiv);

    const player = dashjs.MediaPlayer().create();
    player.updateSettings({

        streaming: {
            abandonLoadTimeout: 10000,
            wallclockTimeUpdateInterval: 100,
            manifestUpdateRetryInterval: 100,
            liveUpdateTimeThresholdInMilliseconds: 0,
            cacheInitSegments: false,
            applyServiceDescription: true,
            applyProducerReferenceTime: true,
            applyContentSteering: true,
            eventControllerRefreshDelay: 100,
            enableManifestDurationMismatchFix: true,
            enableManifestTimescaleMismatchFix: false,
            parseInbandPrft: false,
            capabilities: {
                filterUnsupportedEssentialProperties: true,
                useMediaCapabilitiesApi: false
            },
            timeShiftBuffer: {
                calcFromSegmentTimeline: false,
                fallbackToSegmentTimeline: true
            },
            metrics: {
                maxListDepth: 100
            },
            delay: {
                liveDelayFragmentCount: NaN,
                liveDelay: NaN,
                useSuggestedPresentationDelay: true
            },
            protection: {
                keepProtectionMediaKeys: false,
                ignoreEmeEncryptedEvent: false,
                detectPlayreadyMessageFormat: true,
            },
            buffer: {
                enableSeekDecorrelationFix: false,
                fastSwitchEnabled: true,
                flushBufferAtTrackSwitch: false,
                reuseExistingSourceBuffers: true,
                bufferPruningInterval: 10,
                bufferToKeep: 20,
                bufferTimeAtTopQuality: 30,
                bufferTimeAtTopQualityLongForm: 60,
                initialBufferLevel: NaN,
                stableBufferTime: 12,
                longFormContentDurationThreshold: 600,
                stallThreshold: 0.3,
                useAppendWindow: true,
                setStallState: true,
                avoidCurrentTimeRangePruning: false,
                useChangeTypeForTrackSwitch: true,
                mediaSourceDurationInfinity: true,
                resetSourceBuffersForTrackSwitch: false
            },
            gaps: {
                jumpGaps: true,
                jumpLargeGaps: true,
                smallGapLimit: 1.5,
                threshold: 0.3,
                enableSeekFix: true,
                enableStallFix: false,
                stallSeek: 0.1
            },
            utcSynchronization: {
                enabled: true,
                useManifestDateHeaderTimeSource: true,
                backgroundAttempts: 2,
                timeBetweenSyncAttempts: 30,
                maximumTimeBetweenSyncAttempts: 600,
                minimumTimeBetweenSyncAttempts: 2,
                timeBetweenSyncAttemptsAdjustmentFactor: 2,
                maximumAllowedDrift: 100,
                enableBackgroundSyncAfterSegmentDownloadError: true,
                defaultTimingSource: {
                    scheme: 'urn:mpeg:dash:utc:http-xsdate:2014',
                    value: 'http://time.akamai.com/?iso&ms'
                }
            },
            scheduling: {
                defaultTimeout: 500,
                lowLatencyTimeout: 0,
                scheduleWhilePaused: true
            },
            text: {
                defaultEnabled: true,
                dispatchForManualRendering: false,
                extendSegmentedCues: true,
                imsc: {
                    displayForcedOnlyMode: false,
                    enableRollUp: true
                },
                webvtt: {
                    customRenderingEnabled: false
                }
            },
            liveCatchup: {
                maxDrift: NaN,
                playbackRate: {min: NaN, max: NaN},
                playbackBufferMin: 0.5,
                enabled: null,
            },
            lastBitrateCachingInfo: {enabled: true, ttl: 360000},
            lastMediaSettingsCachingInfo: {enabled: true, ttl: 360000},
            saveLastMediaSettingsForCurrentStreamingSession: true,
            cacheLoadThresholds: {video: 50, audio: 5},
            fragmentRequestTimeout: 20000,
            fragmentRequestProgressTimeout: -1,
            manifestRequestTimeout: 10000,
            retryIntervals: {
                lowLatencyReductionFactor: 10
            },
            retryAttempts: {
                lowLatencyMultiplyFactor: 5
            },
            abr: {
                additionalAbrRules: {
                    insufficientBufferRule: true,
                    switchHistoryRule: true,
                    droppedFramesRule: true,
                    abandonRequestsRule: true
                },
                abrRulesParameters: {
                    abandonRequestsRule: {
                        graceTimeThreshold: 500,
                        abandonMultiplier: 1.8,
                        minLengthToAverage: 5
                    }
                },
                bandwidthSafetyFactor: 0.9,
                useDefaultABRRules: true,
                useDeadTimeLatency: true,
                limitBitrateByPortal: false,
                usePixelRatioInLimitBitrateByPortal: false,
                maxBitrate: {audio: -1, video: -1},
                minBitrate: {audio: -1, video: -1},
                maxRepresentationRatio: {audio: 1, video: 1},
                initialBitrate: {audio: -1, video: -1},
                initialRepresentationRatio: {audio: -1, video: -1},
                autoSwitchBitrate: {audio: true, video: true},
            },
            cmcd: {
                enabled: false,
                sid: null,
                cid: null,
                rtp: null,
                rtpSafetyFactor: 5,
                enabledKeys: ['br', 'd', 'ot', 'tb', 'bl', 'dl', 'mtp', 'nor', 'nrr', 'su', 'bs', 'rtp', 'cid', 'pr', 'sf', 'sid', 'st', 'v']
            },
            cmsd: {
                enabled: false,
                abr: {
                    applyMb: false,
                    etpWeightRatio: 0
                }
            }
        },
        errors: {
            recoverAttempts: {
                mediaErrorDecode: 5
            }

        }
    });
    player.setXHRWithCredentialsForType(true); // Ensure cookies are sent with the request

    // Add Authorization header
    player.extend("RequestModifier", function () {
        return {
            modifyRequestHeader: function (xhr) {
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

export const nextStreams = () => {
    if (currentStreamIndex + maxStreams < filteredStreams.length) {
        currentStreamIndex += maxStreams;
        displayCurrentStreams();
        updateNavigationButtons();
    }
};

export const prevStreams = () => {
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
