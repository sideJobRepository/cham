'use client';

import * as AlertDialog from '@radix-ui/react-alert-dialog';
import { createContext, useContext, useState } from 'react';

type DialogType = 'alert' | 'confirm';

type DialogState = {
  type: DialogType;
  title: string;
  description?: string;
  resolve?: (value: boolean) => void;
};

const DialogContext = createContext<{
  alert: (title: string, description?: string) => void;
  confirm: (title: string, description?: string) => Promise<boolean>;
} | null>(null);

export function DialogProvider({ children }: { children: React.ReactNode }) {
  const [dialog, setDialog] = useState<DialogState | null>(null);

  const alert = (title: string, description?: string) => {
    setDialog({ type: 'alert', title, description });
  };

  const confirm = (title: string, description?: string) => {
    return new Promise<boolean>((resolve) => {
      setDialog({ type: 'confirm', title, description, resolve });
    });
  };

  const close = (result = false) => {
    dialog?.resolve?.(result);
    setDialog(null);
  };

  return (
    <DialogContext.Provider value={{ alert, confirm }}>
      {children}

      {dialog && (
        <AlertDialog.Root
          open={!!dialog}
          onOpenChange={(open) => {
            if (!open) close(false);
          }}
        >
          <AlertDialog.Portal>
            <AlertDialog.Overlay className="dialog-overlay" />
            <AlertDialog.Content className="dialog-content">
              <AlertDialog.Title className="dialog-title">{dialog.title}</AlertDialog.Title>

              {dialog.description && (
                <AlertDialog.Description className="dialog-desc">
                  {dialog.description}
                </AlertDialog.Description>
              )}

              <div className="dialog-actions">
                {dialog.type === 'confirm' && (
                  <AlertDialog.Cancel asChild>
                    <button onClick={() => close(false)}>취소</button>
                  </AlertDialog.Cancel>
                )}

                <AlertDialog.Action asChild>
                  <button onClick={() => close(true)}>확인</button>
                </AlertDialog.Action>
              </div>
            </AlertDialog.Content>
          </AlertDialog.Portal>
        </AlertDialog.Root>
      )}
    </DialogContext.Provider>
  );
}

export const useDialog = () => {
  const ctx = useContext(DialogContext);
  if (!ctx) throw new Error('DialogProvider missing');
  return ctx;
};
