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
        }
    })
})

/**
 * 初始化网页元素摆放
 */
$(function () {
    $('.card .close').on('click',function(){
        $(this).closest('.card').fadeOut();
    })

    let rem = window.getComputedStyle(document.documentElement)["fontSize"];
    let height = parseInt($('footer .card-body').css('height')) + parseInt(rem);
    setTimeout(function () {
        $('footer').css('bottom', -height + 'px').on('mouseover', function () {
            $(this).css('bottom', 0)
        }).on('mouseout', function () {
            $(this).css('bottom', -height + 'px')
        })
    }, 2000)

    let headerHeight = $('header').css('height');
    $('#info').css('top', headerHeight).fadeIn();
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
    // console.log('纠正')
    if ($(wrapper).find('[name=confirmPassword]').attr('disabled'))
        $(wrapper).css('height', $(wrapper).find('.pos').position().top)
    else
        $(wrapper).css('height', parseInt($(wrapper).find('.internalPassword').css('height')) - parseInt($(wrapper).find('.pos').position().top) + 'px')
}

//输入密码后的向下滚动效果
//这个input bug还是挺大的，稍后再说
$('#regPassword,#regPassword [name=confirmPassword]').on('input', function () {
    setTimeout(function () {
        correctWrapperHeight('.doublePassword')
    }, 400)
})

$('#newPassword,#newPassword [name=confirmPassword]').on('input', function () {
    setTimeout(function () {
        correctWrapperHeight('.doublePasswordV2')
    }, 400)
})

$('#regeditModal input:not([name=confirmPassword])').on('blur', function () {
    let valid = $(this).closest('form').valid();
    correctWrapperHeight('.doublePassword')
    if (valid) {
        let height = $(this).closest($('.doublePassword,.doublePasswordV2')).css('height');
        //拉高
        $(this).closest('form').find('.internalPassword').animate({'top': '-' + height}, 500)
        //email只读
        $($(this).closest($('form')).find('input')[0]).attr('readOnly', 'true');
        //移除disabled
        // $(this).closest('form').find('[name=confirmPassword]').removeAttr('disabled')
        //获得焦点，添加required，加定时器是因为不延迟会导致滚动过高
        setTimeout(function (e) {
            $(e).closest('form').find('[name=confirmPassword]').removeAttr('disabled')
            $(e).closest('form').find('[name=confirmPassword]').focus()
        }, 328, this)
    }
})

$('#changePasswordModal input:not([name=confirmPassword])').on('blur', function () {
    let valid = $(this).closest('form').valid();
    correctWrapperHeight('.doublePasswordV2')
    if (valid) {
        let height = $(this).closest($('.doublePassword,.doublePasswordV2')).css('height');
        //拉高
        $(this).closest('form').find('.internalPassword').animate({'top': '-' + height}, 500)
        //email只读
        $($(this).closest($('form')).find('input')[0]).attr('readOnly', 'true');
        //移除disabled
        // $(this).closest('form').find('[name=confirmPassword]').removeAttr('disabled')
        //获得焦点，添加required，加定时器是因为不延迟会导致滚动过高,同时这个定时器的事件也要控制，太快会导致同样的问题
        setTimeout(function (e) {
            $(e).closest('form').find('[name=confirmPassword]').removeAttr('disabled')
            $(e).closest('form').find('[name=confirmPassword]').focus()
        }, 800, this)
    }
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
//FIXME 如果exit不在function里面，就会打开页面直接运行，为什么？
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
    fileGroup.html('文本内容: <br>')
    fileGroup.append(fileItem)
    file.attr('disabled', 'true')
    fileGroup.append('<textarea type="text" name="text" class="d-none">' + tempContent + '</textarea>')
    sendModal.modal('show');
    sendTextModal.find('textarea').val('')
})