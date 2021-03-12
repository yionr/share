/**
 * ajax拦截表单提交
 */

loginModal.find('form').submit(function () {
    if (!$(this).valid())
        return false;
    let email = $('#loginEmail').val();
    loginPassword.attr('disabled','true')
    $(this).ajaxSubmit({
        data: {
            password: CryptoJS.MD5(loginPassword.val()).toString()
        },
        dataType: 'json',
        success: function (data) {
            switch (data.status) {
                case -1:
                    loginModal.modal('hide');
                    newToast(false, '邮箱不存在')
                    //清空表单
                    loginModal.find('form')[0].reset();
                    break;
                case 0:
                    newToast(false, '密码错误')
                    //清空密码框
                    $('#loginPassword').val('')
                    loginPassword.removeAttr('disabled')
                    break;
                case 1:
                    loginModal.modal('hide');
                    newToast(true, '登录成功')
                    localStorage.setItem('email', email)
                    login(email)
                    break;
                case 2:
                    loginModal.modal('hide');
                    newToast(false, '该邮箱未激活，请先前往邮箱激活')
                    break;
            }
            return false;
        },
        error: function () {
            newToast(false,'网络连接异常')
            loginPassword.removeAttr('disabled')
            return false;
        }
    })
    return false;
})

regeditModal.find('form').submit(function () {
    if (moving)
        return false;
    if (!$(this).valid())
        return false;
    if ($('#confirmRegPassword').attr('disabled')){
        if (!moving)
            showConfirmPassword(this,true)
        return false;
    }

    confirmRegPassword.attr('disabled', 'true')
    regPassword.attr('disabled','true')
    $(this).ajaxSubmit({
        data: {
            password: CryptoJS.MD5(regPassword.val()).toString()
        },
        dataType: 'json',
        success: function (data) {
            switch (data.status) {
                case 1:
                    regeditModal.modal('hide')
                    newToast(true, '账号激活邮件已发送至您的邮箱，请及时点击激活！')
                    break;
                case 0:
                    regeditModal.modal('hide')
                    newToast(false, '邮箱已注册')
                    regeditModal.find('form')[0].reset();
                    break;
                case 2:
                    regeditModal.modal('hide')
                    newToast(false, '账号未激活，请及时到您的收件箱点击激活')
                    break;

            }
            return false;
        },
        error: function () {
            newToast(false,'网络连接异常')
            regPassword.removeAttr('disabled')
            rollback(confirmRegPassword,regPassword)
            return false;
        }
    })
    return false;
})


changePasswordModal.find('form').submit(function () {
    if (moving)
        return false;
    if (!$(this).valid())
        return false;
    if ($('#confirmNewPassword').attr('disabled')){
        if (!moving)
            showConfirmPassword(this,false)
        return false;
    }

    $(this).find('[type=password]').attr('disabled','true')
    $(this).ajaxSubmit({
        data: {
            oldPassword: CryptoJS.MD5(oldPassword.val()).toString(),
            newPassword: CryptoJS.MD5(newPassword.val()).toString()
        },
        dataType: 'json',
        success: function (data) {
            switch (data.status) {
                case 1:
                    changePasswordModal.modal('hide');
                    newToast(true, '密码已成功修改，请重新登录！')
                    exit();
                    break;
                case -1:
                    newToast('false', '旧密码错误')
                    $('#oldPassword').removeAttr('readOnly');
                    $('.internalPassword').animate({'top': 0}, 500)
                    confirmNewPassword.attr('disabled', 'true')
                    changePasswordModal.find('form')[0].reset();
                    $('#oldPassword').focus()
                    break;
            }
            return false;
        },
        error: function () {
            changePasswordModal.modal('hide');
            newToast(false,'网络连接异常')
            return false;
        }
    })
    return false;
})










sendModal.find('form').submit(function () {
    if (!$(this).valid())
        return false;
    let data;
    if (filetype.attr('value') === 'text') {
        data = {
            size: tempContent.length * 4,
            times: $('#allowTimes-input').val(),
            filetype: filetype.attr('value')
        }
    } else {
        data = {
            size: file[0].files[0].size,
            times: $('#allowTimes-input').val(),
            filetype: filetype.attr('value')
        }
    }
    $.ajax('checkFile', {
        data: data,
        type: 'post',
        dataType: 'json',
        success: function (data) {
            switch (data.status) {
                case 1:
                    $('#sendBtn').html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span><span id="process"></span>%')
                    $('#sendBtn').attr('disabled', 'true');
                    sendModal.find('form').ajaxSubmit({
                        data: {
                            clientId: localStorage.getItem('clientId')
                        },
                        dataType: 'json',
                        xhr: function () {
                            let myXhr = $.ajaxSettings.xhr();
                            if (myXhr.upload) {
                                myXhr.upload.addEventListener('progress', function (e) {
                                    if (e.lengthComputable) {
                                        let percent = Math.floor(e.loaded / e.total * 100);
                                        $("#process").html(percent);
                                        if (percent === 100){
                                            sendModal.find('.modal-footer small').removeClass('d-none')
                                        }
                                    }
                                }, false);
                            }
                            return myXhr;
                        },
                        success: function (data) {
                            if (data.status.length === 4) {
                                //    得到取件码
                                sendModal.modal('hide')
                                $('#codeNumber').text(data.status)
                                tempCode = data.status;
                                codeModal.find('h5').text('发送成功，您的取件码为')
                                $('.qrAndDl').removeClass('d-none');
                                codeModal.modal('show')
                            } else {
                                if (parseInt(data.status)===0)
                                    newToast(false, '服务器文件保存失败,请稍后重试')
                                else if (parseInt(data.status)===-1)
                                    newToast(false, '本系统目前已没有空余取件码！暂停上传服务。。。感谢支持')
                                sendModal.modal('hide');
                                return false;
                            }
                        },
                        error: function () {
                            newToast(false,'网络连接异常')
                            return false;
                        }
                    });
                    break;
                case -1:
                    sendModal.modal('hide');
                    newToast(false, '非法篡改允许次数，上传失败')
                    break;
                case -2 :
                    sendModal.modal('hide');
                    newToast(false, '文件容量超出上限！')
                    break;
                case -3:
                    sendModal.modal('hide');
                    newToast(false, '用户信息已过期，请重新登录')
                    setTimeout(function () {
                        exit();
                    }, 827)
                    break;
            }
        },
        error: function () {
            newToast(false,'网络连接异常')
            return false;
        }
    })
    return false;   //阻止表单默认提交
})
receiveModal.find('form').submit(function () {
    if (!$(this).valid())
        return false;
    if (check.attr('value') === 'true') {
        $(this).ajaxSubmit({
            dataType: 'json',
            success: function (data) {
                switch (data.status) {
                    case 0:
                        receiveModal.find('[name=password]').val('')
                        newToast(false, '取件码不存在')
                        code.val(''); //包括下面的这几个val作用是修复bug：通过直链下载后，第一次打开接收框会含有上次的取件码
                        break;
                    case 1:
                        switch (data.filetype) {
                            case 'text':
                                //弹出一个显示框，将文本显示在里面
                                $('#textContent').text(data.content)
                                receiveTextModal.modal('show')
                                receiveModal.find('form')[0].reset();
                                break;
                            case 'image':
                                $('.image').find('img').attr('src', 'data:image/png;base64,' + data.content)
                                imageModal.modal('show')
                                receiveModal.find('form')[0].reset();
                                break;
                            case 'file':
                                check.attr('value', 'false')
                                receiveModal.find('form').submit();
                                newToast(true, '取件成功')
                                newToast(true,'请耐心等待云端处理数据，根据待取文件大小的不同，这个时间至多为30秒！')
                                break;
                        }
                        code.val('')
                        receiveModal.modal('hide')
                        break;
                    case 2:
                        receiveModal.modal('show');
                        code.addClass('border-success').attr('readonly', 'true')
                        $('#code_password').removeAttr('disabled')
                        //FIXME 如果不延迟一下的话，好像会被忽略掉，猜测和模态框的动画冲突了，动画一次只能执行一个
                        setTimeout(function () {
                            pw.animate({
                                'height': pw.closest('.modal-body').css('height')
                            }, 472)
                        }, 300)

                        break;
                    case 3:
                        newToast(false, '密码错误')
                        //清空密码
                        receiveModal.find('[name=password]').val('')
                        break;
                    case 4:
                        newToast(false, '文件过期了')
                        receiveModal.modal('hide')
                        break;
                    case -1:
                        newToast(false, '云端文件不见了！请尽快告知管理员')
                        code.val('');
                        receiveModal.modal('hide');
                        break;
                    case -2:
                        newToast(false, '违规操作！')
                        code.val('');
                        receiveModal.modal('hide');
                        break;
                    case 5:
                        newToast(false, '文件下载次数已用完')
                        code.val('');
                        receiveModal.modal('hide');
                        break;
                }
                return false;
            },
            error: function () {
                newToast(false,'网络连接异常')
                return false;
            }
        });
        return false;
    }
    return true;
})