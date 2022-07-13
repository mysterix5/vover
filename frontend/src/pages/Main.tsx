import {Button, Grid} from "@mui/material";
import TextSubmit from "./subcomponents/TextSubmit";
import {useRef, useState} from "react";
import {WordResponse} from "../services/model";
import TextCheck from "./subcomponents/TextCheck";
import Audio from "./subcomponents/Audio";
import {apiGetAudio, apiGetAudioAndPlay} from "../services/apiServices";


export default function Main() {

    const [splitText, setSplitText] = useState<WordResponse[]>();
    const [audioFile, setAudioFile] = useState<any>();

    const audioplayer = useRef<HTMLAudioElement>(null);
    function play(){
        audioplayer.current?.play();
    }
    function pause(){
        audioplayer.current?.pause();
    }

    function isAvailable(availibility: string){
        return availibility==="PUBLIC";
    }

    function checkSplitText(){
        for(const word of splitText!){
            if(!isAvailable(word.availability)){
                return false;
            }
        }
        return true;
    }

    function getAudio(){
        // apiGetAudio(splitText!);
        apiGetAudioAndPlay(splitText!)
            .then(setAudioFile);
    }

    return (
        <Grid container alignContent={"center"} flexDirection={"column"}>
            <Grid item>
                <TextSubmit setSplitText={setSplitText}/>
            </Grid>
            <Grid item>
                {
                    splitText &&
                    <TextCheck splitText={splitText}/>
                }
            </Grid>
            <Grid item>
                {
                    splitText && checkSplitText() &&
                    <Audio getAudio={getAudio}/>
                }
            </Grid>
            <Grid item>
                { audioFile &&
                    <>
                    <audio src={audioFile} autoPlay={false} ref={audioplayer}>

                    </audio>
                    <Button onClick={play}>
                        play
                    </Button>
                    <Button onClick={pause}>
                        pause
                    </Button>
                    </>
                }
            </Grid>
        </Grid>
    )
}