export function getCookie(cname: string) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

export function setCookie(name: string, value: any, options = {}) {
    if (options.expires instanceof Date) {
        options.expires = options.expires.toUTCString();
    }

    let updatedCookie = {
        [encodeURIComponent(name)]: encodeURIComponent(value),
        sameSite: 'strict',
        ...options,
    };

    document.cookie = Object.entries(updatedCookie)
        .map((kv) => kv.join('='))
        .join(';');
}