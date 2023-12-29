$(function () {
    $('.btn-delete').on('click', function () {
        var id = $(this).closest('tr').find('td:first-child').text();
        $('#delete-id').val(id);
        if (confirm('삭제하시겠습니까?')) {
            $('#delete-form').submit();
        }
    });
});
