document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('videoRequestForm');
    form.addEventListener('submit', async function(event) {
        event.preventDefault();

        const cameraName = document.getElementById('cameraName').value;
        const year = document.getElementById('year').value;
        const month = document.getElementById('month').value;
        const day = document.getElementById('day').value;
        const time = document.getElementById('time').value;
        const duration = document.getElementById('duration').value;

        const videoRequest = {
            cameraName: cameraName,
            year: year,
            month: month,
            day: day,
            time: time,
            duration: duration
        };

        try {
            const response = await fetch('http://localhost:8080/api/streamPage/get-video', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
                },
                body: JSON.stringify(videoRequest)
            });

            if (response.ok) {
                const videoBlob = await response.blob();
                const videoUrl = URL.createObjectURL(videoBlob);
                const videoElement = document.getElementById('downloadedVideo');
                videoElement.src = videoUrl;

                const downloadLink = document.getElementById('downloadVideoButton');
                downloadLink.href = videoUrl;
                downloadLink.download = `${cameraName}_${year}_${month}_${day}_${time}.mp4`;
                downloadLink.style.display = 'block';

                document.getElementById('videoContainer').style.display = 'flex';
            } else {
                alert('Failed to download video');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred while downloading the video');
        }
    });
});
