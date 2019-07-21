import { CSSProperties } from 'react';

export const green = {
  color: '#00b200'
};
export const yellow = {
  color: '#a39900'
};
export const red = {
  color: '#c40000'
};
export const gray = {
  color: '#6a6a6a'
};
export const blue = {
  color: '#1890ff'
};
export const lightGreen = {
  color: '#7ab275'
};
export const lightRed = {
  color: '#c46f6b'
};
export const page = {
  margin: '0 70px'
};
export const wrap: CSSProperties = {
  wordBreak: 'break-all',
  wordWrap: 'break-word'
};
export const label: CSSProperties = {
  display: 'inline-block',
  width: '120px',
  textAlign: 'right',
  marginRight: '20px',
  fontWeight: 'bold',
};
export const filter: CSSProperties = {
  marginTop: '10px',
  padding: '10px',
  border: '1px solid #1890ff',
  borderRadius: '9px',
  background: '#eaeeff',
};
export const goalStatus = (status: string): any[] => {
  switch (status.toLowerCase()) {
    case 'compile_error':
      return [red, 'Compile Error', red.color, lightRed.color];
    case 'complete_success':
      return [green, 'Completed with success', green.color, lightGreen.color];
    case 'complete_error':
      return [red, 'Completed with error', red.color, lightRed.color];
    case 'incomplete':
      return [red, 'Incomplete', red.color, lightRed.color];
    default:
      return [gray, 'Unknown', gray.color, gray.color];
  }
};
export const taskStatus = (status: string): any[] => {
  switch (status.toLowerCase()) {
    case 'complete':
      return [green, 'Complete', green.color];
    case 'incomplete':
      return [red, 'Incomplete', red.color];
    default:
      return [gray, 'Unknown', gray.color];
  }
};
export const taskError = (error: string): any[] => {
  switch (error.toLowerCase()) {
    case 'no_error':
      return [green, 'No Error', green.color];
    case 'self_error':
      return [red, 'Self Error', red.color];
    case 'sub_error':
      return [red, 'Sub Error', red.color];
    case 'self_sub_error':
      return [red, 'Self & Sub Error', red.color];
    default:
      return [gray, 'Unknown', gray.color];
  }
};
export const task = (status: string, error: string) => {
  if (status.toLowerCase() === 'complete') {
    if (error.toLowerCase() === 'no_error') {
      return green;
    } else {
      return red;
    }
  } else {
    if (error.toLowerCase() === 'no_error') {
      return red;
    } else {
      return red;
    }
  }
};
export const entity = (level: string) => {
  switch (level.toLowerCase()) {
    case 'error':
      return red;
    case 'warn':
      return yellow;
    default:
      return gray;
  }
};
