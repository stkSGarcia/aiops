/*tslint:disable:no-bitwise*/
export const goalType = (goalFilter): number => {
  let goal = 0;
  goalFilter.forEach(v => {
    switch (v) {
      case 'gi':
        goal |= 1;
        break;
      case 'ge':
        goal |= 2;
        break;
      case 'gs':
        goal |= 4;
        break;
      case 'gce':
        goal |= 8;
        break;
      default:
    }
  });
  return goal === 0 ? -1 : goal;
};

export const timeRange = (date: number, range: number[]): number[] => {
  let dateRange = range;
  if (range[0] === -28800000 && range[1] === -28800000) {
    dateRange = [range[0], range[1] + 86400000];
  }
  return dateRange.map(v => date + v);
};

export const indices = (page: number, pageSize: number): number[] => {
  const begin = pageSize * (page - 1);
  const end = begin + pageSize;
  return [begin, end];
};

export const duration = (durationRange: any): number[] => {
  let minDur = -1;
  let maxDur = -1;
  if (durationRange !== undefined) {
    if (durationRange[0] !== undefined) {
      minDur = durationRange[0];
    }
    if (durationRange[1] !== undefined) {
      maxDur = durationRange[1];
    }
  }
  return [minDur, maxDur];
};
