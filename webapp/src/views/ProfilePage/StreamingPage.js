import React, { useState, useEffect } from "react";
// nodejs library that concatenates classes
import classNames from "classnames";

// chart component
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
} from 'recharts';

import { useLocation } from "react-router-dom";
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


function createData(tripId, start, maxSpeed, avgSpeed, minAltitude, maxAltitude, minSpeed) {
  return { tripId, start, maxSpeed, avgSpeed, minAltitude, maxAltitude, minSpeed};
}


const dashboardRoutes = "/landing-page";

const basicUrl = "https://iot.nonnenmacher.dev/trips/";

// initial table data
const initialDetailData = {
    tripId: "",
    duration: null,
    start: "",
    end: "",
    minAltitude: null,
    maxAltitude: null,
    absElevation: null,
    minSpeed: null,
    maxSpeed: null,
    avgSpeed: null,
}

// initial chart data
const initialChartData = {
    _embedded: {
        dataPointList: [
                         {
                             timestamp: '', cadence: 0, speed: 0, altitude: 0, gear: 0
                         },
                       ]
    }
};

export default function StreamingPage(props) {
    let location = useLocation();
    let id = location.search ? location.search.substr(1) :  "";
    // console.log(location);
    // console.log(id);
    let detailUrl = basicUrl + id;
    let chartUrl = detailUrl + "/data"

  const classes = useStyles();
  const { ...rest } = props;
  const imageClasses = classNames(
    classes.imgRaised,
    classes.imgRoundedCircle,
    classes.imgFluid
  );
  const [detailData, setDetailData] = useState(initialDetailData);
  const [chartRawData, setChartRawData] = useState(initialChartData);
  const [rows, setRows] = useState([]);
  const [chartData, setChartData] = useState([]);

  function getData(url, setFunc) {
      fetch(url)
        .then(res => res.json())
        .then(res => setFunc(res))
        .catch(err => console.log(err))
  }
  useEffect(() => {
  let timer = setInterval(function() {
        getData(detailUrl, setDetailData);
        getData(chartUrl, setChartRawData);
  }, 1000);
   // set table data
      let tempRows = [];
      for (let i in detailData) {
          if (typeof(detailData[i]) === 'number' && detailData[i] > 0) {
              detailData[i] = detailData[i].toFixed(2);
          }
      }
      let row = createData(detailData.tripId, detailData.start, detailData.maxSpeed,
      detailData.avgSpeed, detailData.minAltitude, detailData.maxAltitude, detailData.minSpeed);
      tempRows.push(row);
      setRows(tempRows);
   // set chart data
        let tempChartData = [];
        chartRawData._embedded.dataPointList.map((item, index) => {
            for (let i in item) {
                item[i] = item[i] || 0;
            }
            tempChartData.push(item);
            return tempChartData;
        })
        setChartData(tempChartData);

        return () => {
            clearInterval(timer);
        }
  }, [detailUrl, chartUrl, detailData, chartRawData]);

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
                <h2>Live Cycling Record</h2>
                <GridItem xs={12} sm={12} md={12} className={classes.navWrapper}>
                    <Table className={classes.table} aria-label="customized table">
                        <TableHead>
                            <TableRow>
                                <StyledTableCell>TripId</StyledTableCell>
                                <StyledTableCell  align="right">Start Time</StyledTableCell>
                                <StyledTableCell  align="right">MaxSpeed</StyledTableCell>
                                <StyledTableCell  align="right">MinSpeed</StyledTableCell>
                                <StyledTableCell  align="right">AvgSpeed</StyledTableCell>
                                <StyledTableCell  align="right">MaxAltitude</StyledTableCell>
                                <StyledTableCell  align="right">MinAltitude</StyledTableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {rows.map(row => (
                                <StyledTableRow key={row.tripId}>
                                    <StyledTableCell component="th" scope="row">
                                        {row.tripId}
                                    </StyledTableCell>
                                    <StyledTableCell align="right">{row.start}</StyledTableCell>
                                    <StyledTableCell align="right">{row.maxSpeed}</StyledTableCell>
                                    <StyledTableCell align="right">{row.minSpeed}</StyledTableCell>
                                    <StyledTableCell align="right">{row.avgSpeed}</StyledTableCell>
                                    <StyledTableCell align="right">{row.maxAltitude}</StyledTableCell>
                                    <StyledTableCell align="right">{row.minAltitude}</StyledTableCell>
                                </StyledTableRow>
                            ))}
                        </TableBody>
                    </Table>
                </GridItem>
                <GridItem xs={12} sm={12} md={12} className={classes.navWrapper}>
                    <h3>Cadence[HZ]</h3>
                    <LineChart
                     width={1100}
                     height={300}
                     data={chartData}
                     margin={{ top: 5, right: 30, left: 20, bottom: 5, }}
                    >
                        <CartesianGrid strokeDasharray="3" />
                        <XAxis dataKey="timestamp" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="cadence" stroke="#006600" activeDot={{ r: 8 }} />
                    </LineChart>
                </GridItem>
                <GridItem xs={12} sm={12} md={12} className={classes.navWrapper}>
                    <h3>Speed[m/s]</h3>
                    <LineChart
                        width={1100}
                        height={300}
                        data={chartData}
                        margin={{top: 5, right: 30, left: 20, bottom: 5,}}
                    >
                        <CartesianGrid strokeDasharray="3" />
                        <XAxis dataKey="timestamp" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="speed" stroke="#0033cc" activeDot={{ r: 8 }} />
                    </LineChart>
                </GridItem>
                <GridItem xs={12} sm={12} md={12} className={classes.navWrapper}>
                    <h3>Altitude[m]</h3>
                    <LineChart
                       width={1100}
                       height={300}
                       data={chartData}
                       margin={{top: 5, right: 30, left: 20, bottom: 5,}}
                    >
                        <CartesianGrid strokeDasharray="3" />
                        <XAxis dataKey="timestamp" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="altitude" stroke="#ff0000" activeDot={{ r: 8 }} />
                    </LineChart>
                </GridItem>
            </GridContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
