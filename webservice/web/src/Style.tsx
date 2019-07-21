import { CSSProperties } from 'react';

export const uploadPage = {
  margin: '0 70px',
};
export const block = {
  background: '#ffffff',
  boxShadow: '0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19)',
  margin: '10px',
  padding: '15px',
  borderRadius: '10px',
};
export const showDiv: CSSProperties = {
  visibility: 'visible',
  opacity: 1,
  transition: 'all 0.2s cubic-bezier(0.215, 0.61, 0.355, 1)',
  WebkitTransition: 'all 0.2s cubic-bezier(0.215, 0.61, 0.355, 1)',
};
export const hideDiv: CSSProperties = {
  visibility: 'hidden',
  opacity: 0,
  maxHeight: 0,
  overflow: 'hidden',
  transition: 'all 0.2s cubic-bezier(0.215, 0.61, 0.355, 1)',
  WebkitTransition: 'all 0.2s cubic-bezier(0.215, 0.61, 0.355, 1)',
};
export const foldDiv: CSSProperties = {
  visibility: 'visible',
  opacity: 1,
  overflow: 'hidden',
  transition: 'all 0.2s cubic-bezier(0.215, 0.61, 0.355, 1)',
  WebkitTransition: 'all 0.2s cubic-bezier(0.215, 0.61, 0.355, 1)',
};
export const wordWrap: CSSProperties = {
  margin: 0,
  whiteSpace: 'pre-wrap',
  wordWrap: 'break-word',
  wordBreak: 'break-all',
  tabSize: 8,
};
export const black = {
  color: 'black',
  fontSize: '18px',
};
export const green = {
  color: 'green',
};
export const orange = {
  color: '#ff7214',
};
export const red = {
  color: '#a8403b',
};
export const blue = {
  color: '#000099',
};
export const headCell = {
  background: 'rgba(0, 43, 84, 0.65)',
  color: 'rgba(255, 255, 255, 0.8)',
};
export const rowBackground = (index: number) => {
  return {background: index % 2 === 0 ? '#f8f8ff' : '#dce6ff'};
};
