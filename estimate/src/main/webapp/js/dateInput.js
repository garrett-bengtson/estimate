document.addEventListener('DOMContentLoaded', function () {
	const datePickerElements = document.querySelectorAll('.datetimepicker-group');
    
    if (datePickerElements) {
        const options = {
            display: {
                components: {
                    calendar: true,
                    clock: false
                }
            },
            localization: {
                format: 'MM/dd/yyyy'
            },
            restrictions: {
            },
            dateRange: false,
        };

		datePickerElements.forEach(element => {
			new tempusDominus.TempusDominus(element, options);
		});
    } else {
        console.error('Date picker element not found.');
    }
});