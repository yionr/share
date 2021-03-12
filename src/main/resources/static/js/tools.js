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
    $.ajax('exit', {
        dataType: 'json',
        method: 'POST',
        success: function (data) {
            if (data.status === 0) {
                newToast(true, '已退出登录')
                $('#log-reg-btn').removeClass('d-none');
                $('#logged').addClass('d-none').find('button').text('')
                $('#allowTimes-input').attr('max', 9)
                localStorage.removeItem('email');
            }
        },
        error: function () {
            newToast(false, '网络连接异常')
        }
    })

}

function showConfirmPassword(e, readonly) {
    moving = true;
    let height = $(e).find($('.doublePassword,.doublePasswordV2')).css('height');
    //拉高
    $(e).find('.internalPassword').animate({'top': '-' + height}, 328, 'linear')
    if (readonly)
        $(e).find('#regEmail,#oldPassword').attr('readOnly', 'true');
    //获得焦点，添加required，加定时器是因为不延迟会导致滚动过高
    setTimeout(function (e) {
        $(e).find('[name=confirmPassword]').removeAttr('disabled')
        $(e).find('[name=confirmPassword]').focus()
        moving = false;
        //    timeout 必须比上面拉高速度慢，否则修改密码时会出问题
    }, 600, e)
}

function rollback(elem, select, message) {
    moving = true;
    elem.val('')
    elem.attr('disabled', 'true')
    select.select()
    elem.parent().animate({'top': 0}, 328)
    if (message)
        newToast(false, message)
    setTimeout(function () {
        moving = false;
    }, 328)
}

function guid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        let r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

//localstorage 生成一个clientId=uuid，查看文件会把属于这个client的文件以及当前登录账户的文件列出来。

function listFiles() {
    $.ajax('listFiles', {
        data: {
            clientId: localStorage.getItem('clientId')
        },
        dataType: 'json',
        method: 'post',
        success: function (data) {
            switch (data.status) {
                case -1:
                    newToast(false, '客户端初始化异常，现重新分配clientId')
                    if (!localStorage.getItem('clientId'))
                        localStorage.setItem('clientId', guid())
                    break;
                case 0:
                case 1:
                    let resultArr = eval(data.files);
                    for (let item of resultArr){
                        generateMyFileItem(item['fid'],item['name'],item['password'],item['times'],item['uid'],item['filetype'],item['leftTime'])
                    }
            }
        //files:
        },
        error: function () {
            newToast(false, '网络异常，无法加载我的文件列表')
        }
    })
}

function generateMyFileItem(fid, name, password, times, user, filetype, lastTime) {
    if (password.length === 0)
        password = '无'
    if (user === -1){
        user = '游客'
        lastTime = toLastTime(604800000 - lastTime);
    }
    else{
        user = '当前登录用户'
        //30天的毫秒数 - lastTime
        lastTime = toLastTime(2592000000 - lastTime);
    }
    if (filetype === 'file')
        filetype = '普通文件'
    else if (filetype === 'text')
        filetype = '文本'
    else
        filetype = '图片'
    let item = $('<tr>\n' +
        '                            <th scope="row">' + fid + '</th>\n' +
        '                            <td>' + name + '</td>\n' +
        '                            <td>' + password + '</td>\n' +
        '                            <td>' + times + '</td>\n' +
        '                            <td>' + user + '</td>\n' +
        '                            <td>' + filetype + '</td>\n' +
        '                            <td>' + lastTime + '</td>\n' +
        '                            <td><button type="button" class="close"><span>&times;</span></button></td>\n' +
        '                        </tr>')
    let table = $('#myFileList table');
    table.append(item);
    $('#myFileList table .close').on('click',function () {
        if (deleteFile($(this).closest('tr').find('[scope=row]').text()))
            $(this).closest('tr').remove()
    })
}

function toLastTime(time) {
    if (time < 0)
        return '已过期'
    else if (time < 60000)
        return Math.round(time / 1000) + '秒'
    else if (time < 3600000)
        return Math.round(time / 1000 / 60) + '分'
    else if (time < 86400000)
        return Math.round(time /1000 / 60 / 60) + '时'
    else
        return Math.round(time / 1000 / 60 / 60 / 24) + '天'
}


function deleteFile(fid) {
    let result = false;
    $.ajax('deleteFile', {
        async: false,
        method: 'post',
        data: {
            fid: fid,
            clientId: localStorage.getItem('clientId')
        },
        dataType: 'json',
        success: function (data) {
            switch (data.status) {
                case -1:
                    newToast(false, '该文件不属于你！')
                    break;
                case 0:
                    newToast(false, '删除失败')
                    break;
                case 1:
                    newToast(true, '删除成功')
                    result = true;
                    break;
                //        把那个文件item去除
            }
        },
        error: function () {
            newToast(false, '网络连接失败')
            result = false
        }
    })
    return result;
}