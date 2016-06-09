(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('MensajeDialogController', MensajeDialogController);

    MensajeDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Mensaje', 'User'];

    function MensajeDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Mensaje, User) {
        var vm = this;

        vm.mensaje = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.users = User.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.mensaje.id !== null) {
                Mensaje.update(vm.mensaje, onSaveSuccess, onSaveError);
            } else {
                Mensaje.save(vm.mensaje, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ecofruitApp:mensajeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.fecha = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
