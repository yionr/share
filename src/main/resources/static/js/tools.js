
/**
 * 新建一个toast
 * 第一个参数确定toast属于提示框还是警告框，第二个参数表示内容，提示框delay为5秒，警告框为15秒
 */
function newToast(isOK, content) {
    let title, img, delay;
    if (isOK)
        img = 'img/r.png'
    else
        img = 'img/e.png'
    title = isOK ? '提示' : '警告';
    delay = isOK ? 5000 : 15000;
    let toast = $('<div class="toast" role="alert" data-delay="' + delay + '">\n' +
        '        <div class="toast-header">\n' +
        '            <img src="' + img + '" class="rounded" alt="" width="30" height="30">\n' +
        '            <strong class="mr-auto">' + title + '</strong>\n' +
        '            <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">\n' +
        '                <span aria-hidden="true">&times;</span>\n' +
        '            </button>\n' +
        '        </div>\n' +
        '        <div class="toast-body">\n' +
        '            ' + content + '\n' +
        '        </div>\n' +
        '    </div>')
    toastContainerBR.append(toast);
    toast.toast('show');
    setTimeout(function () {
        toast.remove()
    }, delay + 1000)
}

function login(email) {
    $('#log-reg-btn').addClass('d-none');
    $('#logged').removeClass('d-none').find('button').text(email);
    $('#allowTimes-input').attr('max', 99)
}

function exit() {
    $.ajax('exit.do', {
        method: 'POST',
        success: function (data) {
            if (JSON.parse(data).status === 0) {
                newToast(true, '已退出登录')
                $('#log-reg-btn').removeClass('d-none');
                $('#logged').addClass('d-none').find('button').text('');
                $('#allowTimes-input').attr('max', 9)
                localStorage.removeItem('email');
            }
        }
    })

}