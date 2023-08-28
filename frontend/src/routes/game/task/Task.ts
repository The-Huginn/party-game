export type Task = {
    task: string;
    timer: Timer;
}

type Timer = {
    duration: number;
    delay: number;
    autostart: boolean;
}