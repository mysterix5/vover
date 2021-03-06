import {Grid} from "@mui/material";
import TextSubmit from "./subcomponents/TextSubmit";
import {useState} from "react";
import {TextResponse} from "../services/model";
import TextCheck from "./subcomponents/TextCheck";
import Audio from "./subcomponents/Audio";
import {apiGetAudio} from "../services/apiServices";
import {isAvailable} from "./subcomponents/helpers";


export default function Main() {
    const [splitText, setSplitText] = useState<TextResponse>();
    const [audioFile, setAudioFile] = useState<any>();
    const [ids, setIds] = useState<string[]>([])

    function checkSplitText(){
        for(const word of splitText!.textWords){
            if(!isAvailable(word.availability)){
                return false;
            }
        }
        return true;
    }

    function getAudio(){
        apiGetAudio(ids)
            .then(setAudioFile);
    }


    return (
        <Grid container alignItems={"center"} flexDirection={"column"}>
            <Grid item>
                <TextSubmit setSplitText={setSplitText} setIds={setIds}/>
            </Grid>
            <Grid item ml={2} mr={2}>
                {
                    splitText &&
                    <TextCheck splitText={splitText} ids={ids} setIds={setIds}/>
                }
            </Grid>
            <Grid item>
                {
                    splitText && checkSplitText() &&
                    <Audio getAudio={getAudio} audioFile={audioFile}/>
                }
            </Grid>
        </Grid>
    )
}