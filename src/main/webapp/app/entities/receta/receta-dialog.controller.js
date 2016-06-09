(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('RecetaDialogController', RecetaDialogController);

    RecetaDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Receta', 'Comentario', 'User'];

    function RecetaDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Receta, Comentario, User) {
        var vm = this;

        vm.receta = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.comentarios = Comentario.query();
        vm.users = User.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.receta.id !== null) {
                Receta.update(vm.receta, onSaveSuccess, onSaveError);
            } else {
                Receta.save(vm.receta, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ecofruitApp:recetaUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


        vm.setImagen = function ($file, receta) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        receta.imagen = base64Data;
                        receta.imagenContentType = $file.type;
                    });
                });
            }
        };
        vm.datePickerOpenStatus.fecha = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
