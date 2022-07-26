import {
    Box,
    Grid
} from "@mui/material";
import {TextMetadataResponse, WordAvail} from "../../services/model";
import WordDropdown from "./WordDropdown";

interface TextCheckProps {
    textMetadataResponse: TextMetadataResponse,
    ids: string[],
    setIds: (ids: string[]) => void
}

export default function TextCheck(props: TextCheckProps) {

    function generateIdSetter(index: number){
        return (id: string) => {
            let localIds = props.ids;
            localIds[index] = id;
            props.setIds(localIds);
        }
    }

    function getWordButton(word: WordAvail, index: number) {
        let myColor: string = "#fff";
        // let myTextDecoration: string = "none";

        if (word.availability === "PUBLIC") {
            myColor = "#12670c";
        } else if (word.availability === "INVALID") {
            myColor = "#881111";
            // myTextDecoration = "line-through";
        } else if (word.availability === "NOT_AVAILABLE") {
            myColor = "#b43535";
        }

        return (
            <Box sx={{backgroundColor: myColor}}>
                <WordDropdown wordAvail={word} setIdInArray={generateIdSetter(index)} choicesList={props.textMetadataResponse.wordRecordMap[word.word]} id={props.ids[index]}/>
            </Box>
        )
    }

    return (
        <>
            <Grid container justifyContent={"center"}>
                {
                    props.textMetadataResponse &&
                    props.textMetadataResponse!.textWords.map((r, i) =>
                        <Grid item key={i} margin={0.5}>
                            {getWordButton(r, i)}
                        </Grid>
                    )}
            </Grid>
        </>
    )
}