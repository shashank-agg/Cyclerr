import React, { useState, useEffect } from "react";
// nodejs library that concatenates classes
import classNames from "classnames";
import { Link } from "react-router-dom";
// @material-ui/core components
// import table elements
import { withStyles, makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
// core components
import Header from "components/Header/Header.js";
import GridContainer from "components/Grid/GridContainer.js";
import GridItem from "components/Grid/GridItem.js";
import HeaderLinks from "components/Header/HeaderLinks.js";
import Parallax from "components/Parallax/Parallax.js";
import UseFetch from "components/api/useFetch.js";

import profile from "assets/img/faces/shashank.png";

import styles from "assets/jss/material-kit-react/views/profilePage.js";

const useStyles = makeStyles(styles);
const StyledTableCell = withStyles(theme => ({
  head: {
    backgroundColor: theme.palette.common.black,
    color: theme.palette.common.white,
  },
  body: {
    fontSize: 14,
  },
}))(TableCell);

const StyledTableRow = withStyles(theme => ({
  root: {
    '&:nth-of-type(odd)': {
      backgroundColor: theme.palette.background.default,
    },
  },
}))(TableRow);


function createData(tripId, start, end, status) {
  return { tripId, start, end, status };
}

/*const rows = [
  createData('Minimum Speed', 159),
  createData('Maximum Speed', 237),
  createData('Average Speed', 262),
  createData('Minimum Altitude', 305),
  createData('Maximum Altitude', 356),
  createData('Absolute Elevation', 356),
];*/

const dashboardRoutes = "/landing-page";

const listUrl = "https://iot.nonnenmacher.dev/trips/";

const initialData = {
        tripModelList: [{
            tripId: "",
            duration: "",
            start: "",
            end: "",
            status: "",
            maxSpeed: null,
            avgSpeed: "",
            minAltitude: null,
            maxAltitude: null,
            absElevation: null,
            minSpeed: null,
            _links : {
                self: {
                    href: "",
                },
                data: {
                    href: ""
                }
            }
        }]
    }

export default function ProfilePage(props) {
  const classes = useStyles();
  const { ...rest } = props;
  const imageClasses = classNames(
    classes.imgRaised,
    classes.imgRoundedCircle,
    classes.imgFluid
  );
  const [rows, setRows] = useState([]);
  let listData = UseFetch(listUrl);
  useEffect(() => {
    let tempRows = [];
    let data = Object.keys(listData).length === 0 ? initialData : listData._embedded;
    data.tripModelList.map((item, index) => {
        if (index < 10) {
            let row = createData(item.tripId, item.start, item.end, item.status);
            tempRows.push(row);
        }
        return tempRows;
    })
    setRows(tempRows);
  }, [listData]);

  return (
    <div>
      <Header
        color="transparent"
        routes={dashboardRoutes}
        brand="Cyclerr"
        rightLinks={<HeaderLinks />}
        fixed
        changeColorOnScroll={{
          height: 200,
          color: "white"
        }}
        {...rest}
      />
      <Parallax small filter image={require("assets/img/bike-bg0.jpg")} />
      <div className={classNames(classes.main, classes.mainRaised)}>
        <div>
          <div className={classes.container}>
            <GridContainer justify="center">
              <GridItem xs={12} sm={12} md={6}>
                <div className={classes.profile}>
                  <div>
                    <img src={profile} alt="..." className={imageClasses} />
                  </div>
                  <div className={classes.name}>
                    <h3 className={classes.title}>Shashank Aggarwal</h3>
                  </div>
                </div>
              </GridItem>
            </GridContainer>
            <GridContainer justify="center">
              <GridItem xs={12} sm={12} md={8} className={classes.navWrapper}>
                <h2>Cycling Report</h2>
                <Table className={classes.table} aria-label="customized table">
                    <TableHead>
                       <TableRow>
                         <StyledTableCell>TripId</StyledTableCell>
                         <StyledTableCell  align="right">Start Time</StyledTableCell>
                         <StyledTableCell  align="right">End Time</StyledTableCell>
                         <StyledTableCell  align="right">Status</StyledTableCell>
                       </TableRow>
                    </TableHead>
                    <TableBody>
                       {rows.map(row => (
                         <StyledTableRow key={row.tripId}>
                           <StyledTableCell component="th" scope="row">
                           <Link to={{pathname: row.status === "PENDING" ? "/streaming-page" : "/detail", search: row.tripId}}>{row.tripId}</Link>
                           </StyledTableCell>
                           <StyledTableCell align="right">{row.start}</StyledTableCell>
                           <StyledTableCell align="right">{row.end}</StyledTableCell>
                           <StyledTableCell align="right">{row.status}</StyledTableCell>
                         </StyledTableRow>
                       ))}
                    </TableBody>
                </Table>
              </GridItem>
            </GridContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
