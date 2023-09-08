export type taskType = 'SINGLE' | 'DUO' | 'ALL';
export type Task = {
    task: string;
    task_type: taskType;
    player: string;
    timer?: Timer;
    pairs?: TeamPair[];
}

export type TeamPair = {
    first: string;
    second: string;
}

export type Timer = {
    duration: number;
    delay?: number;
    autostart: boolean;
    initialDuration: number;
}