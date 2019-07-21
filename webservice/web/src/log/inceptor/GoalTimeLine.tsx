import { GoalTaskThinBean, TaskBean } from '../conf/DataStructure';
import vis from 'vis/dist/vis';

export const goaltimelineData = (targetGoal: GoalTaskThinBean): any => {
  const items = {};
  let minTime = 0;
  let maxTime = 0;
  var subtask: any = [];

  const setRange = (task: TaskBean) => {
    if (minTime === 0 && maxTime === 0) {
      minTime = task.startTime;
      maxTime = task.endTime;
    } else {
      if (task.startTime < minTime) {
        minTime = task.startTime;
      }
      if (task.endTime > maxTime) {
        maxTime = task.endTime;
      }
    }
  };

  if (targetGoal !== undefined) {
    if (targetGoal.tasks !== null) {
      if (items[targetGoal.id] === undefined) {
        items[targetGoal.id] = [];
      }
      var num = 0;
      targetGoal.tasks.forEach((task1, index1) => {
        setRange(task1);
        items[targetGoal.id].push({
          id: num,
          group: targetGoal.id,
          key: String(index1),
          // content: goal.name,
          name: task1.name,
          start: task1.startTime,
          end: task1.endTime,
        });
        num += 1;
        subtask = task1;
        var mark = 0;
        while (subtask.subTasks !== null && mark === 0) {
          subtask.subTasks.forEach((task2, index2) => {
            setRange(task2);
            items[targetGoal.id].push({
              id: num,
              group: targetGoal.id,
              key: String(index1 + '-' + index2),
              // content: goal.name,
              name: task2.name,
              start: task2.startTime,
              end: task2.endTime,
            });
            num += 1;
            mark = 1;
            if (task2.subTasks !== null) {
              subtask = task2.subTasks;
              mark = 0;
            }
          });
        }
      });
    } else {
      const task = targetGoal;
      if (items[targetGoal.id] === undefined) {
        items[targetGoal.id] = [];
      }
      items[targetGoal.id].push({
        id: 0,
        group: targetGoal.id,
        key: '0',
        // content: goal.name,
        name: task.name,
        start: task.startTime,
        end: task.endTime,
      });
    }
  }
  return {
    items: items,
    min: minTime,
    max: maxTime,
  };
};

export const dataSet = (items: {}, targetGoal: GoalTaskThinBean): vis.DataSet[] => {
  const itemDataSet = new vis.DataSet();
  const tasks = items[targetGoal.id];
  if (tasks !== undefined) {
    tasks.forEach(task => itemDataSet.add({
      id: task.id,
      group: task.group,
      key: task.key,
      name: task.name,
      start: task.start,
      end: task.end,
      style: task.style,
    }));
  }
  return [itemDataSet];
};
