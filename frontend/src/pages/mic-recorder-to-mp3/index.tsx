// import MicRecorder from 'mic-recorder-to-mp3';
import {Button, Typography} from "@mui/material";

export default function Mic(){
    function record(){

// New instance
//         const recorder = new MicRecorder({
//             bitRate: 128,
//             encoder: 'mp3', // default is mp3, can be wav as well
//             sampleRate: 44100, // default is 44100, it can also be set to 16000 and 8000.
//         });
//
// // Start recording. Browser will request permission to use your microphone.
//         recorder.start().then(() => {
//             // something else
//         }).catch((e) => {
//             console.error(e);
//         });
//
// // Once you are done singing your best song, stop and get the mp3.
//         recorder
//             .stop()
//             .getAudio()
//             .then(([buffer, blob]) => {
//                 // do what ever you want with buffer and blob
//                 // Example: Create a mp3 file and play
//                 const file = new File(buffer, 'me-at-thevoice.mp3', {
//                     type: blob.type,
//                     lastModified: Date.now()
//                 });
//
//                 const player = new Audio(URL.createObjectURL(file));
//                 player.play();
//
//             }).catch((e) => {
//             alert('We could not retrieve your message');
//             console.log(e);
//         });
    }

    return (
        <>
            <Typography>mic record</Typography>
            <Button onClick={record}>record</Button>
        </>
    )
}