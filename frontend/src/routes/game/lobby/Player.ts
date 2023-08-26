class Player {
    id!: number;
    name!: string;

    public constructor(init?: Partial<Player>) {
        Object.assign(this, init);
    }
}

export default Player;