import * as React from 'react';
import * as Style from '../conf/LogStyle';
import * as ReactDOMServer from 'react-dom/server';
import * as moment from 'moment';
import Config from '../../Config';
import vis from 'vis/dist/vis';
import { GoalTaskThinBean, SessionGoalBean, SessionInfoBean } from '../conf/DataStructure';

export const timelineData = (data: SessionGoalBean[], startIndex: number, targetSession?: SessionGoalBean, targetGoal?: string): any => {
  const items = {}; // sessionid -> goals[]
  const groups: any[] = [];
  let minTime = 0;
  let maxTime = 0;

  const setRange = (goal: GoalTaskThinBean) => {
    if (minTime === 0 && maxTime === 0) {
      minTime = goal.startTime;
      maxTime = goal.endTime;
    } else {
      if (goal.startTime < minTime) {
        minTime = goal.startTime;
      }
      if (goal.endTime > maxTime) {
        maxTime = goal.endTime;
      }
    }
  };

  const getTitle = (goal: GoalTaskThinBean): JSX.Element => {
    const color = Style.goalStatus(goal.goalStatus);
    const desc = goal.desc;
    const name = desc.substring(0, desc.indexOf('\n'));
    const sql = desc.substring(desc.indexOf('sql:\n') + 5, desc.indexOf('pre mark:'));
    return (
      <div>
        <h3>{goal.name}</h3>
        <p style={{fontSize: '12px'}}><span style={{fontWeight: 'bold'}}>Desc: </span>{name}</p>
        <p style={{fontSize: '12px'}}>
          <span style={{fontWeight: 'bold'}}>Duration: </span>
          {goal.duration} ms&nbsp;
          ({moment(goal.startTime).format(Config.DATE_TIME_FORMAT)} ~ {moment(goal.endTime).format(Config.DATE_TIME_FORMAT)})
        </p>
        <p style={{fontSize: '12px'}}><span style={{fontWeight: 'bold'}}>Goal Status: </span><span style={color[0]}>{color[1]}</span></p>
        <div style={{fontSize: '12px'}}>
          <span style={{fontWeight: 'bold'}}>SQL: </span>
          {sql.split('\n').map((value, i) => <p style={{margin: 0}} key={i}>{value}</p>)}
        </div>
      </div>
    );
  };

  let sessionList = data;

  if (targetSession !== undefined) {
    targetSession.goals.forEach(goal => {
      setRange(goal);
      const color = Style.goalStatus(goal.goalStatus);
      const title = getTitle(goal);
      if (items[targetSession.id] === undefined) {
        items[targetSession.id] = [];
      }
      items[targetSession.id].push({
        id: goal.id,
        group: targetSession.id,
        // content: goal.name,
        start: goal.startTime,
        end: goal.endTime,
        style: goal.id === targetGoal ?
          `color: #ffffff; background-color: ${Style.blue.color}; border-color: ${Style.blue.color}; cursor: pointer;` :
          `color: #ffffff; background-color: ${color[3]}; border-color: ${color[2]}; cursor: pointer;`,
        title: ReactDOMServer.renderToString(title),
      });
    });
    groups.push({
      id: targetSession.id,
      content: `Target Session`,
      title: `Session ID: ${targetSession.id}`,
      style: `cursor: pointer;`,
    });
    sessionList = sessionList.filter(value => value.id !== targetSession.id);
  }

  sessionList
  // .filter(value => value.goals !== undefined && value.goals.length !== 0)
    .forEach((value, index) => {
      value.goals.forEach(goal => {
        setRange(goal);
        const color = Style.goalStatus(goal.goalStatus);
        const title = getTitle(goal);
        if (items[value.id] === undefined) {
          items[value.id] = [];
        }
        items[value.id].push({
          id: goal.id,
          group: value.id,
          // content: goal.name,
          start: goal.startTime,
          end: goal.endTime,
          style: `color: #ffffff; background-color: ${color[3]}; border-color: ${color[2]}; cursor: pointer;`,
          title: ReactDOMServer.renderToString(title),
        });
      });
      groups.push({
        id: value.id,
        content: `Session ${startIndex + index}`,
        title: `Session ID: ${value.id}`,
        style: `cursor: pointer;`,
      });
    });

  return {
    items: items,
    groups: groups,
    min: minTime,
    max: maxTime,
  };
};

export const dataSet = (items: {}, groups: any[]): vis.DataSet[] => {
  const itemDataSet = new vis.DataSet();
  const groupDataSet = new vis.DataSet();
  groups.forEach((value, index) => {
    groupDataSet.add({
      id: value.id,
      order: index,
      content: value.content,
      title: value.title,
      style: value.style,
    });
    const goals = items[value.id];
    if (goals !== undefined) {
      goals.forEach(goal => itemDataSet.add({
        id: goal.id,
        group: goal.group,
        content: goal.content,
        start: goal.start,
        end: goal.end,
        style: goal.style,
        title: goal.title,
      }));
    }
  });
  return [itemDataSet, groupDataSet];
};

export const sessionData = (data: SessionInfoBean[]): vis.DataSet => {
  const sessionItems = new vis.DataSet();
  data.forEach(value => {
    const title: JSX.Element = (
      <div>
        <h3>Session ID: {value.id}</h3>
        <p style={{fontSize: '12px'}}>
          <span style={{fontWeight: 'bold'}}>Start Time: </span>
          {moment(value.endTime).format(Config.DATE_TIME_FORMAT)}
        </p>
        <p style={{fontSize: '12px'}}>
          <span style={{fontWeight: 'bold'}}>End Time: </span>
          {moment(value.endTime).format(Config.DATE_TIME_FORMAT)}
        </p>
      </div>
    );
    sessionItems.add({
      id: value.id,
      content: `Session ${value.id.substring(0, 5)}...`,
      start: moment(value.startTime),
      end: moment(value.endTime),
      style: `cursor: pointer;`,
      title: ReactDOMServer.renderToString(title),
    });
  });
  return sessionItems;
};
