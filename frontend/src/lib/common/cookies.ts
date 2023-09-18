export function getCookie(cname: string) {
    if (typeof window === 'undefined') {
        console.log('unable to get cookie');
        return;
    }
    
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
    return null;
}

export function setCookie(name: string, value: any, options = {}) {
    if (typeof window === 'undefined') {
        console.log('unable to set cookie ' + name + ' with value ' + value);
        return;
    }
    
    if (options.expires instanceof Date) {
        options.expires = options.expires.toUTCString();
    }

    options.path = '/';

    let updatedCookie = {
        [encodeURIComponent(name)]: encodeURIComponent(value),
        sameSite: 'strict',
        ...options,
    };

    document.cookie = Object.entries(updatedCookie)
        .map((kv) => kv.join('='))
        .join(';');
}