import * as React from 'react';
import * as Style from '../../Style';
import * as JstackStyle from '../conf/JstackStyle';

const headLineHighlight = (head: string, index: number): JSX.Element => {
  const split = (): string[] => {
    const labels = ['NEW', 'RUNNABLE', 'BLOCKED', 'TIMED_WAITING', 'WAITING'];
    for (let i = 0; i < labels.length; i++) {
      const start = head.indexOf(labels[i]);
      if (start !== -1) {
        const end = start + labels[i].length;
        return [head.substring(0, start), head.substring(start, end), head.substring(end)];
      }
    }
    return [];
  };

  const parts = split();
  if (parts.length !== 0) {
    const style = JstackStyle.stateStyle(parts[1])[0];
    return (
      <span key={index}>
        {parts[0]}
        <span style={{fontWeight: 'bold', color: style}}>{parts[1]}</span>
        {parts[2]}
        {'\n'}
      </span>
    );
  } else {
    return <span key={index}>{head}{'\n'}</span>;
  }
};

const highlight = (line: string, index: number): JSX.Element => {
  const leftParenthesis = line.indexOf('(');
  if (line.includes(':', leftParenthesis)) {
    return (
      <span key={index}>
        {line.substring(0, leftParenthesis)}
        <span style={JstackStyle.method}>{line.substring(leftParenthesis)}</span>
        {'\n'}
      </span>
    );
  } else {
    return <span key={index}>{line}{'\n'}</span>;
  }
};

export const TraceHighlighter = (lines: string): JSX.Element => (
  <p style={Style.wordWrap}>
    {lines.split('\n').map((line, index) => index === 0
      ? headLineHighlight(line, index)
      : highlight(line, index))}
  </p>
);

export default TraceHighlighter;
