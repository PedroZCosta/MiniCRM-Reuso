using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers
{
    public class TokenRecuperacaoController : Controller
    {
        private readonly MiniCrmContext _context;

        public TokenRecuperacaoController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: TokenRecuperacao
        public async Task<IActionResult> Index()
        {
            var miniCrmContext = _context.TokenRecuperacao.Include(t => t.IdUsuarioNavigation);
            return View(await miniCrmContext.ToListAsync());
        }

        // GET: TokenRecuperacao/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var tokenRecuperacao = await _context.TokenRecuperacao
                .Include(t => t.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdToken == id);
            if (tokenRecuperacao == null)
            {
                return NotFound();
            }

            return View(tokenRecuperacao);
        }

        // GET: TokenRecuperacao/Create
        public IActionResult Create()
        {
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email");
            return View();
        }

        // POST: TokenRecuperacao/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("IdToken,CodigoHash,CriadoEm,ExpiraEm,Usado,IdUsuario")] TokenRecuperacao tokenRecuperacao)
        {
            if (ModelState.IsValid)
            {
                _context.Add(tokenRecuperacao);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", tokenRecuperacao.IdUsuario);
            return View(tokenRecuperacao);
        }

        // GET: TokenRecuperacao/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var tokenRecuperacao = await _context.TokenRecuperacao.FindAsync(id);
            if (tokenRecuperacao == null)
            {
                return NotFound();
            }
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", tokenRecuperacao.IdUsuario);
            return View(tokenRecuperacao);
        }

        // POST: TokenRecuperacao/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("IdToken,CodigoHash,CriadoEm,ExpiraEm,Usado,IdUsuario")] TokenRecuperacao tokenRecuperacao)
        {
            if (id != tokenRecuperacao.IdToken)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(tokenRecuperacao);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!TokenRecuperacaoExists(tokenRecuperacao.IdToken))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", tokenRecuperacao.IdUsuario);
            return View(tokenRecuperacao);
        }

        // GET: TokenRecuperacao/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var tokenRecuperacao = await _context.TokenRecuperacao
                .Include(t => t.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdToken == id);
            if (tokenRecuperacao == null)
            {
                return NotFound();
            }

            return View(tokenRecuperacao);
        }

        // POST: TokenRecuperacao/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var tokenRecuperacao = await _context.TokenRecuperacao.FindAsync(id);
            if (tokenRecuperacao != null)
            {
                _context.TokenRecuperacao.Remove(tokenRecuperacao);
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool TokenRecuperacaoExists(int id)
        {
            return _context.TokenRecuperacao.Any(e => e.IdToken == id);
        }
    }
}
