/**
 * 为客户端设置一个uuid
 */
$(function () {
    if (!localStorage.getItem('clientId')) {
        localStorage.setItem('clientId',guid())
    }
})




/**
 * 自动登录
 */
$(function () {
    $.ajax('login', {
        method: 'POST',
        dataType: 'json',
        success: function (data) {
            switch (data.status) {
                case 1:
                    let email = localStorage.getItem('email');
                    if (email !== null) {
                        login(email);
                    }
                    break;
                case 0:
                    newToast(false, '上次登录后密码已修改，请重新登录')
                    //清空session和localStorage
                    if (localStorage.getItem('email') !== null) {
                        localStorage.removeItem('email')
                    }
                    break;
            }
        },
        error: function () {
            newToast(false,'网络连接异常,自动登录失败，请手动登录或刷新页面')
        }
    })
})

/**
 * 初始化网页元素摆放
 */
$(function () {
    if (!window.localStorage.getItem('readed')) {
        title.html('风享站<span class="badge badge-success">New</span>')
    }
    title.on('click',function () {
        $('#info').toggleClass('d-none')
    })

    $('#info .card-body').on('scroll',function () {
        if($(this)[0].scrollHeight === $(this)[0].offsetHeight + $(this).scrollTop()){
            window.localStorage.setItem('readed','true')
            title.html('风享站')
        }
    })
    $('.card .close').on('click',function(){
        $('#info').addClass('d-none');
    })

})


/**
 * 带参url检测
 */
$(function () {
    let url = window.location.href;
    let urlParam = url.split('\?')[1];
    if (!urlParam)
        return;
    if (urlParam.startsWith('email')) {
        let email = urlParam.split('&')[0];
        let uuid = urlParam.split('&')[1];
        $.ajax('active', {
            method: 'POST',
            data: {
                email: email.split('=')[1],
                uuid: uuid.split('=')[1]
            },
            dataType: 'json',
            success: function (data) {
                switch (data.status) {
                    case 1:
                        //激活成功，提示并自动登录
                        newToast(true, '激活成功，将为您自动登录');
                        //将邮箱存入localStorage
                        localStorage.setItem('email', email.split('=')[1])
                        setTimeout(function () {
                            window.location.href = url.split('\?')[0]
                        }, 1000)
                        break;
                    case -1:
                        newToast(false, '激活链接过期了，请重新注册！')
                        setTimeout(function () {
                            window.location.href = url.split('\?')[0]
                        }, 3000)
                        break;
                    case 0:
                    case -2:
                    case -3:
                        window.location.href = url.split('\?')[0]
                        break;
                }
            },
            error: function () {
                newToast(false,'网络连接异常，激活失败，请重试')
            }
        })
    } else if (urlParam.startsWith('code')) {
        let tempCode = urlParam.split('=')[1];
        code.val(tempCode);
        receiveModal.find('form').attr('action', 'download/' + tempCode)
        receiveModal.find('form').submit();
        history.pushState(null, '', '/');
    }
})

/**
 * 初始化剪切板
 **/
$(function () {
    new ClipboardJS('#copyCode');
    new ClipboardJS('#copyText');
    new ClipboardJS('#directLink', {
        container: codeModal[0],
        text: function () {
            return window.location.href.split('\?')[0] + $('#codeNumber').text();
        }
    });
})

/**
 * 初始化发送框
 * */
$(function () {
    send.on('mouseover', function () {
        $(this).text('文件')
        text.removeClass('d-none');
        image.removeClass('d-none');
    })
    send.parent().on('mouseleave', function () {
        send.text('发送')
        text.addClass('d-none');
        image.addClass('d-none');
    })
    // 不同按钮点开，界面不一样。
    //点击文本则展示一个文本域
    text.on('click', function () {
        filetype.attr('value', 'text');
        sendTextModal.modal('show');
    })
    //点击文件则直接让用户选择文件
    send.on('click', function () {
        filetype.attr('value', 'file');
        file.removeAttr('accept')
        file.trigger('click')
    })
    image.on('click', function () {
        filetype.attr('value', 'image');
        file.attr('accept', 'image/*')
        file.trigger('click')
    })
})












/**
 * 当输入取件码时，同步改变action
 */
code.on('change', function () {
    receiveModal.find('form').attr('action', 'download/' + code.val())
})

//复制、QR码的点击
$('#copyCode,#copyText').on('click', function () {
    //    文本变为复制成功，颜色变为绿success 然后渐变回原本的颜色
    $(this).text('复制成功').css('backgroundColor', "#28a745")
})
$('#directLink').on('click', function () {
    newToast(true, '复制成功')
})
$('#getQRCode').on('click', function () {
    new QRCode($('#QRCode')[0], window.location.href.split('\?')[0] + $('#codeNumber').text());
    codeModal.modal('hide');
    QRCodeModal.modal('show');
})

function correctWrapperHeight(wrapper) {
    if ($(wrapper).find('[name=confirmPassword]').attr('disabled'))
        $(wrapper).css('height', $(wrapper).find('.pos').position().top)
    else
        $(wrapper).css('height', parseInt($(wrapper).find('.internalPassword').css('height')) - parseInt($(wrapper).find('.pos').position().top) + 'px')
}

//表单校验是在blur的时候进行的，同时会跳转可视窗口的高度，以免看不到label
$('#regeditModal input').on('blur', function () {
    correctWrapperHeight('.doublePassword')
})

$('#changePasswordModal input').on('blur', function () {
    correctWrapperHeight('.doublePasswordV2')
})

/**
 * 选择文件后显示模态框，并显示文件名
 */
file.on('change', function () {
    //C:\fakepath\接收.png
    let absoluteFileName = $(this).val().split('\\');
    //接收.png
    let fileName = absoluteFileName[absoluteFileName.length - 1]
    if (filetype.attr('value') === 'image')
        if (!/\.(gif|svg|bmp|png|jpeg|jpg|wepb|ico|tif)$/i.test(fileName)) {
            //    警告不是图片文件，并return
            newToast(false, '该文件不是图片，请重新选择，或使用‘文件’通道上传')
            return;
        }
    //当有文件之后，显示模态框，并将数据显示
    $(this).next('.fileGroup').append($('<div class="fileItem">' + fileName + '</div>'));
    sendModal.modal('show');
})
/**
 * 退出登录
 */
$('#exitLogin').on('click', function () {
    exit()
})


sendTextModal.find('.modal-footer').find('button').on('click', function () {
    let fileGroup = $('.fileGroup');
    tempContent = sendTextModal.find('textarea').val();
    if (tempContent.trim().length === 0) {
        newToast(false, '内容请不要为空')
        //TODO 拒绝关闭，抖动窗口
        return false;
    }
    let temp = tempContent.length > 10 ? tempContent.substr(0, 10) + '...' : tempContent;
    let fileItem = $('<div class="fileItem text-left"> ' + temp + '</div>')
    fileGroup.html('文本内容摘要: <br>')
    fileGroup.append(fileItem)
    file.attr('disabled', 'true')
    fileGroup.append('<textarea type="text" name="text" class="d-none">' + tempContent + '</textarea>')
    sendModal.modal('show');
    sendTextModal.find('textarea').val('')
})

$('#reflush').on('click',function() {
    $('#myFileList tbody').html('');
    listFiles();
})