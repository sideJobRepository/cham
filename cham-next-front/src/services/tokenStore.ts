
let accessToken: string | null = null;

export const tokenStore = {
    get(): string | null {
        return accessToken;
    },
    set(token: string) {
        accessToken = token;
    },
    clear() {
        accessToken = null;
    },
};