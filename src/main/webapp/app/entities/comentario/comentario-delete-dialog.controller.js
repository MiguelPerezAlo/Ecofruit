(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('ComentarioDeleteController',ComentarioDeleteController);

    ComentarioDeleteController.$inject = ['$uibModalInstance', 'entity', 'Comentario'];

    function ComentarioDeleteController($uibModalInstance, entity, Comentario) {
        var vm = this;

        vm.comentario = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Comentario.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
