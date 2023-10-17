/**
 * AJAX call management
 */

function makeCall(method, url, formElement, callback, reset = true) {
    const req = new XMLHttpRequest(); // visible by closure
    req.onreadystatechange = function() {
        callback(req)
    }; // closure
    req.open(method, url);
    if (formElement == null) {
        req.send();
    } else if(formElement instanceof FormData) {
        req.send(formElement);
    } else {
        req.send(new FormData(formElement));
        if ( reset === true) {
            formElement.reset();
        }
    }
}