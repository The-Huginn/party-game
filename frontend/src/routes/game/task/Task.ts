export type Task = {
    task: string;
    timer?: Timer;
}

export type Timer = {
    duration: number;
    delay: number;
    initialDuration: number;
}