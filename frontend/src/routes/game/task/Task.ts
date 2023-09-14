export type taskType = 'SINGLE' | 'DUO' | 'ALL';
export type Task = {
    task: string;
    task_type: taskType;
    player: string;
    price: Price;

    timer?: Timer;
    pairs?: TeamPair[];
}

export type Price = {
    enabled: boolean;
    price: number;
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