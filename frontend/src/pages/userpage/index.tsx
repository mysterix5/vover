import {Box, Grid, Tab, Typography} from "@mui/material";
import Recordings from "./Recordings";
import {TabContext, TabList, TabPanel} from "@mui/lab";
import {SyntheticEvent, useEffect, useState} from "react";
import History from "./History";
import {useAuth} from "../../usermanagement/AuthProvider";
import {useNavigate, useParams} from "react-router-dom";
import Friends from "./Friends";

const categoryChoices = ["recordings", "history", "users"];

export default function UserPage() {
    const [tabValue, setTabValue] = useState("");

    const {username} = useAuth();

    const {category} = useParams();
    const nav = useNavigate();

    useEffect(() => {
        if (category && categoryChoices.includes(category)) {
            setTabValue(category);
        }else{
            setTabValue("recordings")
        }
    }, [category])

    useEffect(() => {
        if (!localStorage.getItem("jwt")) {
            nav("/login")
        }
    }, [nav])

    const handleChange = (event: SyntheticEvent, newValue: string) => {
        setTabValue(newValue);
    };

    return (
        <>
            <Grid container justifyContent={"center"}>
                <Grid item border={3} borderRadius={5} borderColor={"rosybrown"}>
                    <Typography ml={1} mr={1} mt={0.3} mb={0.3}
                                variant={"h4"} color={"rosybrown"}>
                        {username}
                    </Typography>
                </Grid>
            </Grid>
            { tabValue &&
                <Box sx={{width: '100%', typography: 'body1'}}>
                    <TabContext value={tabValue}>
                        <Box sx={{borderBottom: 1, borderColor: 'divider'}}>
                            <TabList onChange={handleChange} aria-label="lab API tabs example">
                                <Tab label="Recordings" value="recordings"/>
                                <Tab label="History" value="history"/>
                                <Tab label="Users" value="users"/>
                            </TabList>
                        </Box>
                        <TabPanel value="recordings">
                            <Recordings/>
                        </TabPanel>
                        <TabPanel value="history">
                            <History/>
                        </TabPanel>
                        <TabPanel value="users">
                            <Friends/>
                        </TabPanel>
                    </TabContext>
                </Box>
            }
        </>
    )
}