/**
 * 初始化internalPassword的高度
 */
regeditModal.on('shown.bs.modal', function () {
    halfHeight = parseInt($('#regeditModal .internalPassword').css('height')) / 2
    $('.doublePassword').css('height', halfHeight + 'px')
})
changePasswordModal.on('shown.bs.modal', function () {
    halfHeight = parseInt($('#changePasswordModal .internalPassword').css('height')) / 2
    $('.doublePasswordV2').css('height', halfHeight + 'px')
})
/**
 * 添加快捷键
 */
$('.modal:not(#sendTextModal)').on('show.bs.modal', function () {
    $(this).bind('keypress', this, function (e) {
        if (e.key === 'Enter') {
            //默认情况下，即使提交表单也不会让原本focus的input 失去焦点
            $(e.data).find('input:focus').blur();
            //默认情况下，表单里面enter会触发submit
            //FIXME 奇了怪了，文本modal enter明明都不会啊
            return true;
        }
    })
}).on('hide.bs.modal', function () {
    $(this).off('keypress')
})

/**
 *关闭模态框的时候重置表单,清空所有内容
 */
$('.modal').on('hidden.bs.modal', function () {
    $(this).find('label.error').remove()
    $(this).find('input').removeClass('error')
})
regeditModal.on('hidden.bs.modal', function () {
    $(this).find('.internalPassword').css('top', '0')
    confirmRegPassword.attr('disabled', 'true')
    regPassword.removeAttr('disabled')
    $(this).find('#regEmail').removeAttr('readOnly')
    $(this).find('form')[0].reset();
})
loginModal.on('hidden.bs.modal', function () {
    $(this).find('form')[0].reset();
    loginPassword.removeAttr('disabled')
})
changePasswordModal.on('hidden.bs.modal', function () {
    $(this).find('.internalPassword').css('top', '0')
    oldPassword.removeAttr('readOnly');
    oldPassword.removeAttr('disabled')
    newPassword.removeAttr('disabled')
    confirmNewPassword.attr('disabled', 'true')
    $(this).find('form')[0].reset();
})
sendModal.on('hidden.bs.modal', function () {
    $(this).find('form')[0].reset();
    $(this).find('.fileGroup').html('文件列表: <br>')
    file.removeAttr('disabled')
    $('#sendBtn').html('发送')
    $('#sendBtn').removeAttr('disabled');
    $(this).find('.modal-footer small').addClass('d-none')
})
sendTextModal.on('hidden.bs.modal', function () {
    $(this).find('textarea').val('')
})
receiveModal.on('hidden.bs.modal', function () {
    $(this).find('form')[0].reset();
    $(this).find('.pw').css('height', 0);
    code.removeClass('border-success').removeAttr('readonly');
    $('#code_password').attr('disabled', 'true')
    $(this).find('form').attr('action', '')
    check.attr('value', 'true')
    code.val('');
})
codeModal.on('hidden.bs.modal', function () {
    $('#copy').text('复制').css('backgroundColor', "#007bff")
    $('.qrAndDl').addClass('d-none')
    $('#codeNumber').text('')
})
receiveTextModal.on('hidden.bs.modal', function () {
    $('#copy').text('复制').css('backgroundColor', "#007bff")
    $('#textContent').text('')
})
QRCodeModal.on('hidden.bs.modal', function () {
    $('#QRCode').html('');
    $('.qrAndDl').removeClass('d-none')
    $('#codeNumber').text(tempCode)
    codeModal.modal('show');
})