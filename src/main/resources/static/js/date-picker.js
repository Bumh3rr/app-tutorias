document.addEventListener('DOMContentLoaded', function () {
    const esLocale = {
        firstDayOfWeek: 1,
        weekdays: {
            shorthand: ['Dom','Lun','Mar','Mié','Jue','Vie','Sáb'],
            longhand: ['Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado']
        },
        months: {
            shorthand: ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'],
            longhand: ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto',
                       'Septiembre','Octubre','Noviembre','Diciembre']
        }
    };

    document.querySelectorAll('input[data-datepicker]').forEach(function (input) {
        flatpickr(input, {
            locale: esLocale,
            dateFormat: 'Y-m-d',
            allowInput: true,
        });
    });

    document.querySelectorAll('input[data-datepicker-range]').forEach(function (input) {
        const startHidden = document.getElementById(input.dataset.rangeStart);
        const endHidden   = document.getElementById(input.dataset.rangeEnd);

        flatpickr(input, {
            locale: esLocale,
            mode: 'range',
            dateFormat: 'Y-m-d',
            onChange: function (selectedDates) {
                if (startHidden) startHidden.value = selectedDates[0] ? selectedDates[0].toISOString().split('T')[0] : '';
                if (endHidden)   endHidden.value   = selectedDates[1] ? selectedDates[1].toISOString().split('T')[0] : '';
            }
        });
    });
});
