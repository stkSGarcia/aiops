import { CSSProperties } from 'react';

export const red = {
  color: 'rgba(224, 54, 54, 0.9)',
};
export const green = {
  color: 'rgba(86, 163, 108, 0.9)',
};
export const yellow = {
  color: 'rgba(234, 207, 2, 0.9)',
};
export const blue = {
  color: 'rgba(51, 135, 196, 0.9)',
};
export const orange = {
  color: 'rgba(192, 122, 40, 0.9)',
};
export const grey = {
  color: 'rgba(106, 106, 106, 0.9)',
};
export const method = {
  color: '#294de0',
};
export const lockLink = {
  padding: '0 5px',
  color: '#002140',
  textDecoration: 'underline',
};
export const threadLink: CSSProperties = {
  color: '#002140',
  padding: '0 10px',
  textDecoration: 'underline',
};
export const stateStyle = (state: string): any[] => {
  switch (state.toUpperCase()) {
    case 'NEW':
      return [blue.color, 'table_row_blue', blue];
    case 'RUNNABLE':
      return [green.color, 'table_row_green', green];
    case 'BLOCKED':
      return [red.color, 'table_row_red', red];
    case 'WAITING':
      return [yellow.color, 'table_row_yellow', yellow];
    case 'TIMED_WAITING':
      return [orange.color, 'table_row_orange', orange];
    default:
      return [grey.color, 'table_row_default', grey];
  }
};
